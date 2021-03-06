package x7c1.linen.repository.unread

import android.app.{Activity, LoaderManager}
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasChannelStatusKey
import x7c1.linen.repository.channel.unread.ChannelSelectable
import x7c1.linen.repository.entry.unread.{EntryAccessor, EntryRowContent, EntrySourcePositionsFactory, UnreadDetail, UnreadOutline}
import x7c1.linen.repository.source.unread.SourceNotLoaded.{Abort, ErrorEmpty}
import x7c1.linen.repository.source.unread.{ClosableSourceAccessor, SourceNotLoaded, SourceRowContent, UnreadSource, UnreadSourceAccessor}
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
  private lazy val sourceUnderlying = {
    database.selectorOf[SourceRowContent].createHolder
  }
  def sources: UnreadSourceAccessor = sourceUnderlying

  private lazy val outlineUnderlying = {
    database.selectorOf[EntryRowContent[UnreadOutline]].createBinder
  }
  def outlines: EntryAccessor[UnreadOutline] = outlineUnderlying

  private lazy val detailUnderlying = {
    database.selectorOf[EntryRowContent[UnreadDetail]].createBinder
  }
  def details: EntryAccessor[UnreadDetail] = detailUnderlying

  def reload[A: HasChannelStatusKey: ChannelSelectable]
    (key: A)(onLoad: LoadCompleteEvent[A] => Unit): Unit = {

//    val first = for {
//      sourceIds <- startLoadingSources(account.accountId)
//      remaining <- loadSourceEntries(sourceIds)
//      _ <- Future { notifyChanged() }
//    } yield {
//      remaining
//    }
//    first onComplete loadNext


    val accountId = implicitly[HasChannelStatusKey[A]].toId(key).accountId
    Log info s"[init] account:$accountId"

    val load = for {
      accessor <- startLoadingSources(key)
      event <- loadSourceEntries(
        remainingSources = accessor map (_.sources) getOrElse Seq()
      )
      _ <- Future {
        close()
        accessor foreach sourceUnderlying.updateSequence
        updateAccessors(event)
      }
      _ <- Future { onLoad(LoadCompleteEvent(key)) }
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
      sourceUnderlying.close()
    }
  }
  private def startLoadingSources[A: HasChannelStatusKey]
    (key: A): Future[Option[ClosableSourceAccessor]] = loaderFactory asFuture {

    AccessorLoader.inspectSourceAccessor(database, key) match {
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
        sourceUnderlying addLength sources.length
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
  def inspectSourceAccessor[A: HasChannelStatusKey](
    db: SQLiteDatabase, key: A): Either[SourceNotLoaded, ClosableSourceAccessor] = {

    try UnreadSourceAccessor.create(db, key) match {
      case Failure(exception) => Left(Abort(exception))
      case Success(accessor) => Right(accessor)
    } catch {
      case e: Exception => Left(Abort(e))
    }
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
