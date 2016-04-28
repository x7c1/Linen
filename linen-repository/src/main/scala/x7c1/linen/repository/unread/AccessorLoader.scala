package x7c1.linen.repository.unread

import android.app.{Activity, LoaderManager}
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.account.AccountIdentifiable
import x7c1.linen.repository.channel.unread.ChannelSelectable
import x7c1.linen.repository.entry.unread.{ClosableEntryAccessor, EntryAccessor, EntryAccessorBinder, EntrySourcePositionsFactory, FooterContent, UnreadDetail, UnreadEntry, UnreadEntryRow, UnreadOutline}
import x7c1.linen.repository.source.unread.SourceNotLoaded.{Abort, ErrorEmpty}
import x7c1.linen.repository.source.unread.{UnreadSource, ClosableSourceAccessor, SourceFooterContent, SourceNotLoaded, UnreadSourceAccessor, UnreadSourceRow}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.patch.FiniteLoaderFactory
import x7c1.wheat.modern.patch.TaskAsync.after

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


class AccessorLoader private (
  database: SQLiteDatabase,
  context: Context,
  loaderManager: LoaderManager ){

  private val outlineAccessors = ListBuffer[ClosableEntryAccessor[UnreadOutline]]()
  private val detailAccessors = ListBuffer[ClosableEntryAccessor[UnreadDetail]]()
  private val loaderFactory = new FiniteLoaderFactory(
    context = context,
    loaderManager = loaderManager,
    startLoaderId = 0
  )
  private var sourceAccessor: Option[ClosableSourceAccessor] = None
  private var currentSourceLength: Int = 0

  def createSourceAccessor: UnreadSourceAccessor = {
    val underlying = new UnreadSourceAccessor {
      override def findAt(position: Int) = {
        sourceAccessor.flatMap(_ findAt position)
      }
      override def positionOf(sourceId: Long): Option[Int] = {
        sourceAccessor.flatMap(_ positionOf sourceId)
      }
      override def length: Int = currentSourceLength
    }
    new SourceFooterAppender(underlying)
  }
  private lazy val outlineUnderlying = new EntryAccessorBinder(outlineAccessors)

  def createOutlineAccessor: EntryAccessor[UnreadOutline] = {
    new EntriesFooterAppender(outlineUnderlying)
  }

  private lazy val detailUnderlying = new EntryAccessorBinder(detailAccessors)

  def createDetailAccessor: EntryAccessor[UnreadDetail] = {
    new EntriesFooterAppender(detailUnderlying)
  }
  def startLoading[A: ChannelSelectable]
    (account: AccountIdentifiable, channel: A)
      (onLoad: LoadCompleteEvent[A] => Unit): Unit = {

//    val first = for {
//      sourceIds <- startLoadingSources(account.accountId)
//      remaining <- loadSourceEntries(sourceIds)
//      _ <- Future { notifyChanged() }
//    } yield {
//      remaining
//    }
//    first onComplete loadNext

    val select = implicitly[ChannelSelectable[A]]
    for {
      accessor <- startLoadingSources(
        accountId = account.accountId,
        channelId = select channelIdOf channel
      )
      event <- loadSourceEntries(
        remainingSources = accessor map (_.sources) getOrElse Seq()
      )
      _ <- Future {
        this.sourceAccessor = accessor
        updateAccessors(event)
      }
      _ <- Future { onLoad(LoadCompleteEvent(channel)) }
    } yield {
      event
    }
  }
  def restartLoading[A: ChannelSelectable]
    (account: AccountIdentifiable, channel: A)
      (onLoad: LoadCompleteEvent[A] => Unit): Unit = {

    val select = implicitly[ChannelSelectable[A]]
    val load = for {
      accessor <- startLoadingSources(account.accountId, select channelIdOf channel)
      event <- loadSourceEntries(
        remainingSources = accessor map (_.sources) getOrElse Seq()
      )
      _ <- Future {
        close()
        this.sourceAccessor = accessor
        updateAccessors(event)
      }
      _ <- Future { onLoad(LoadCompleteEvent(channel)) }
    } yield {
      event.remainingSources
    }
    load onComplete {
      case Success(sources) =>
        Log info s"[done] remains:${sources.length}"
      case Failure(e) =>
        Log error format(e, depth = 30){"[failed]"}
    }
  }

  def close(): Unit = {
    loaderFactory.close()

    synchronized {
      outlineUnderlying.close()
      detailUnderlying.close()
      sourceAccessor foreach (_.close())

      currentSourceLength = 0
      sourceAccessor = None
      outlineAccessors.clear()
      detailAccessors.clear()
    }
  }
  private def startLoadingSources(
    accountId: Long, channelId: Long): Future[Option[ClosableSourceAccessor]] = loaderFactory asFuture {

    AccessorLoader.inspectSourceAccessor(database, accountId, channelId: Long) match {
      case Left(error: ErrorEmpty) =>
        Log error error.message
        Seq()
        None
      case Left(empty) =>
        Log info empty.message
        Seq()
        None
      case Right(accessor) =>
        Some(accessor)
    }
  }
  private def loadSourceEntries(remainingSources: Seq[UnreadSource]) =
    loaderFactory asFuture {
      Log info s"[init] remains:${remainingSources.length}"

      val (sources, remains) = remainingSources splitAt 50
      val (outlines, details) =
        if (sources.nonEmpty) {
          val positions = {
            val factory = new EntrySourcePositionsFactory(database)
            factory create sources
          }
          val outlines = Option(EntryAccessor.forEntryOutline(database, sources, positions))
          val details = Option(EntryAccessor.forEntryDetail(database, sources, positions))
          outlines -> details
        } else {
          None -> None
        }

      AccessorsLoadedEvent(
        loadedSources = sources,
        remainingSources = remains,
        outlines = outlines,
        details = details
      )
    }

  private def updateAccessors(event: AccessorsLoadedEvent) = {
    Log info s"[init] sources.length:${event.loadedSources.length}"

    val sources = event.loadedSources
    if (sources.nonEmpty){
      val positions = {
        val factory = new EntrySourcePositionsFactory(database)
        factory create sources
      }
      val outlines = EntryAccessor.forEntryOutline(database, sources, positions)
      val details = EntryAccessor.forEntryDetail(database, sources, positions)
      synchronized {
        currentSourceLength += sources.length
        outlineAccessors += outlines
        detailAccessors += details
      }
    }
  }
  private def loadNext: Try[AccessorsLoadedEvent] => Unit = {
    case Success(event) => loadMore(event.remainingSources)
    case Failure(e : IllegalStateException) =>

      /*
        known exceptions
        - attempt to re-open an already-closed object:
          SQLiteDatabase: /data/user/0/x7c1.linen/databases/linen-db
        - Cannot perform this operation because the connection pool has been closed.
      */
      Log info e.toString

    case Failure(e) => Log error format(e, depth = 30){"failed"}
  }
  private def loadMore(remaining: Seq[UnreadSource]): Unit = {
    Log info s"[init] remaining:${remaining.length}"
    remaining match {
      case Seq() => Log info "[done]"
      case _ => after(msec = 50){
        loadAndUpdate(remaining) onComplete loadNext
      }
    }
  }
  private def loadAndUpdate(remaining: Seq[UnreadSource]) = for {
    event <- loadSourceEntries(remaining)
    _ <- Future { updateAccessors(event) }
  } yield event
}

object AccessorLoader {
  def apply(database: SQLiteDatabase, activity: Activity): AccessorLoader = {
    new AccessorLoader(
      database,
      activity,
      activity.getLoaderManager
    )
  }
  def inspectSourceAccessor(
    db: SQLiteDatabase, accountId: Long,
    channelId: Long): SourceNotLoaded Either ClosableSourceAccessor =

    try UnreadSourceAccessor.create(db, accountId, channelId) match {
      case Failure(exception) => Left(Abort(exception))
      case Success(accessor) => Right(accessor)
    } catch {
      case e: Exception => Left(Abort(e))
    }
}

case class AccessorsLoadedEvent(
  loadedSources: Seq[UnreadSource],
  remainingSources: Seq[UnreadSource],
  outlines: Option[ClosableEntryAccessor[UnreadOutline]],
  details: Option[ClosableEntryAccessor[UnreadDetail]]
)

case class LoadCompleteEvent[A: ChannelSelectable](channel: A){
  private lazy val select = implicitly[ChannelSelectable[A]]

  def channelId: Long = select channelIdOf channel

  def channelName: String = select nameOf channel
}

private class SourceFooterAppender(
  accessor: UnreadSourceAccessor) extends UnreadSourceAccessor {

  override def findAt(position: Int) = {
    if (isLast(position)){
      Some(UnreadSourceRow(SourceFooterContent()))
    } else {
      accessor findAt position
    }
  }
  override def positionOf(sourceId: Long) = accessor positionOf sourceId

  override def length: Int = {
    // +1 to append Footer
    accessor.length + 1
  }
  private def isLast(position: Int) = position == accessor.length
}

private class EntriesFooterAppender[A <: UnreadEntry](
  accessor: EntryAccessor[A]) extends EntryAccessor[A]{

  override def findAt(position: Int) = {
    if (isLast(position)){
      Some(UnreadEntryRow(FooterContent()))
    } else {
      accessor.findAt(position)
    }
  }
  override def length = {
    // +1 to append Footer
    accessor.length + 1
  }
  override def findKindAt(position: Int) = {
    if (isLast(position)){
      Some(FooterKind)
    } else {
      accessor findKindAt position
    }
  }
  override def firstEntryPositionOf(sourceId: Long) = {
    accessor firstEntryPositionOf sourceId
  }
  private def isLast(position: Int) = position == accessor.length

  override def lastEntriesTo(position: Int) = {
    accessor lastEntriesTo position
  }
}
