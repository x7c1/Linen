package x7c1.linen.repository.unread

import android.app.{Activity, LoaderManager}
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{HasAccountId, HasChannelId}
import x7c1.linen.repository.channel.unread.ChannelSelectable
import x7c1.linen.repository.entry.unread.{EntryAccessor, EntryRowContent, EntrySourcePositionsFactory, UnreadDetail, UnreadOutline}
import x7c1.linen.repository.source.unread.SourceNotLoaded.{Abort, ErrorEmpty}
import x7c1.linen.repository.source.unread.{ClosableSourceAccessor, SourceFooterContent, SourceNotLoaded, UnreadSource, UnreadSourceAccessor}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.patch.FiniteLoaderFactory
import x7c1.wheat.modern.patch.TaskAsync.after

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


class AccessorLoader private (
  database: SQLiteDatabase,
  context: Context,
  loaderManager: LoaderManager ){

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

  private lazy val outlineUnderlying = {
    database.selectorOf[EntryRowContent[UnreadOutline]].createBinder
  }
  def createOutlineAccessor: EntryAccessor[UnreadOutline] = {
    outlineUnderlying
  }

  private lazy val detailUnderlying = {
    database.selectorOf[EntryRowContent[UnreadDetail]].createBinder
  }
  def createDetailAccessor: EntryAccessor[UnreadDetail] = {
    detailUnderlying
  }

  def reload[A: HasAccountId, B: ChannelSelectable]
    (account: A, channel: B)(onLoad: LoadCompleteEvent[B] => Unit): Unit = {

//    val first = for {
//      sourceIds <- startLoadingSources(account.accountId)
//      remaining <- loadSourceEntries(sourceIds)
//      _ <- Future { notifyChanged() }
//    } yield {
//      remaining
//    }
//    first onComplete loadNext

    val accountId = implicitly[HasAccountId[A]] toId account
    Log info s"[init] account:$accountId"

    val load = for {
      accessor <- startLoadingSources(account, channel)
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
    }
  }
  private def startLoadingSources[A: HasAccountId, B: HasChannelId]
    (account: A, channel: B): Future[Option[ClosableSourceAccessor]] = loaderFactory asFuture {

    AccessorLoader.inspectSourceAccessor(database, account, channel) match {
      case Left(error: ErrorEmpty) =>
        Log error error.message
        None
      case Left(empty) =>
        Log info empty.message
        None
      case Right(accessor) =>
        Some(accessor)
    }
  }
  private def loadSourceEntries(remainingSources: Seq[UnreadSource]) =
    loaderFactory asFuture {
      Log info s"[init] remains:${remainingSources.length}"

      val (sources, remains) = remainingSources splitAt 50
      AccessorsLoadedEvent(
        loadedSources = sources,
        remainingSources = remains
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
        outlineUnderlying append outlines
        detailUnderlying append details
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
  def inspectSourceAccessor[A: HasAccountId, B: HasChannelId](
    db: SQLiteDatabase, accountId: A,
    channelId: B): SourceNotLoaded Either ClosableSourceAccessor =

    try UnreadSourceAccessor.create(db, accountId, channelId) match {
      case Failure(exception) => Left(Abort(exception))
      case Success(accessor) => Right(accessor)
    } catch {
      case e: Exception => Left(Abort(e))
    }
}

case class AccessorsLoadedEvent(
  loadedSources: Seq[UnreadSource],
  remainingSources: Seq[UnreadSource]
)

case class LoadCompleteEvent[A: ChannelSelectable](channel: A){
  private lazy val select = implicitly[ChannelSelectable[A]]

  def channelName: String = select nameOf channel
}

private class SourceFooterAppender(
  accessor: UnreadSourceAccessor) extends UnreadSourceAccessor {

  override def findAt(position: Int) = {
    if (isLast(position)){
      Some(SourceFooterContent())
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
