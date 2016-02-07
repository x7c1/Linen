package x7c1.linen.modern.init.unread

import android.app.LoaderManager
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.AccountAccessor.findCurrentAccountId
import x7c1.linen.modern.accessor.ChannelAccessor.findCurrentChannelId
import SourceNotLoaded.{Abort, AccountNotFound, ChannelNotFound, ErrorEmpty}
import x7c1.linen.modern.accessor.{EntryAccessor, EntryAccessorBinder, UnreadSourceAccessor}
import x7c1.linen.modern.init.unread.AccessorLoader.inspectSourceAccessor
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline, UnreadSource}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.patch.FiniteLoaderFactory
import x7c1.wheat.modern.patch.TaskAsync.after

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class AccessorLoader(
  database: SQLiteDatabase,
  layout: MainLayout,
  loaderManager: LoaderManager ){

  private val outlineAccessors = ListBuffer[EntryAccessor[EntryOutline]]()
  private val detailAccessors = ListBuffer[EntryAccessor[EntryDetail]]()
  private val loaderFactory = new FiniteLoaderFactory(
    context = layout.itemView.context,
    loaderManager = loaderManager,
    startLoaderId = 0
  )
  private var sourceAccessor: Option[UnreadSourceAccessor] = None
  private var currentSourceLength: Int = 0

  def createSourceAccessor: UnreadSourceAccessor =
    new UnreadSourceAccessor {
      override def findAt(position: Int): Option[UnreadSource] = {
        sourceAccessor.flatMap(_ findAt position)
      }
      override def positionOf(sourceId: Long): Option[Int] = {
        sourceAccessor.flatMap(_ positionOf sourceId)
      }
      override def length: Int = currentSourceLength
    }

  def createOutlineAccessor: EntryAccessor[EntryOutline] =
    new EntryAccessorBinder(outlineAccessors)

  def createDetailAccessor: EntryAccessor[EntryDetail] =
    new EntryAccessorBinder(detailAccessors)

  def startLoading(): Unit = {
    val first = for {
      sourceIds <- startLoadingSources()
      remaining <- loadSourceEntries(sourceIds)
      _ <- Future { notifyChanged() }
    } yield {
      remaining
    }
    first onComplete loadNext
  }

  def close(): Unit = {
    loaderFactory.close()

    synchronized {
      currentSourceLength = 0
      sourceAccessor = None
      outlineAccessors.clear()
      detailAccessors.clear()
    }
  }
  private def startLoadingSources(): Future[Seq[Long]] = loaderFactory asFuture {
    inspectSourceAccessor(database).toEither match {
      case Left(error: ErrorEmpty) =>
        Log error error.message
        Seq()
      case Left(empty) =>
        Log info empty.message
        Seq()
      case Right(accessor) =>
        this.sourceAccessor = Some(accessor)
        accessor.sourceIds
    }
  }
  private def loadSourceEntries(remainingSourceIds: Seq[Long]) = loaderFactory asFuture {
    val (sourceIds, remains) = remainingSourceIds splitAt 50
    if (sourceIds.nonEmpty){
      val positions = EntryAccessor.createPositionMap(database, sourceIds)
      val outlines = EntryAccessor.forEntryOutline(database, sourceIds, positions)
      val details = EntryAccessor.forEntryDetail(database, sourceIds, positions)
      synchronized {
        currentSourceLength += sourceIds.length
        outlineAccessors += outlines
        detailAccessors += details
      }
    }
    remains
  }
  private def loadNext: Try[Seq[Long]] => Unit = {
    case Success(ids) => loadMore(ids)
    case Failure(e : IllegalStateException) =>

      /*
        known exceptions
        - attempt to re-open an already-closed object:
          SQLiteDatabase: /data/user/0/x7c1.linen/databases/linen-db
        - Cannot perform this operation because the connection pool has been closed.
      */
      Log info e.toString

    case Failure(e) => Log error formatError(e)
  }
  private def loadMore(remaining: Seq[Long]): Unit = {
    Log info s"[init] remaining(${remaining.length})"
    remaining match {
      case Seq() => Log info "[done]"
      case _ => after(msec = 50){
        loadSourceEntries(remaining) onComplete loadNext
      }
    }
  }
  private def notifyChanged() = layout.itemView runUi { _ =>

    /*
    2015-12-20:
    it should be written like:
      layout.sourceList.getAdapter.notifyItemRangeInserted(0, ...)

    but this 'notifyItemRangeInserted' causes following error (and crash)
      java.lang.IndexOutOfBoundsException:
        Inconsistency detected. Invalid view holder adapter positionViewHolder
    */

    layout.sourceList.getAdapter.notifyDataSetChanged()
    layout.entryList.getAdapter.notifyDataSetChanged()
    layout.entryDetailList.getAdapter.notifyDataSetChanged()

    Log info "[done]"
  }
  private def formatError(e: Throwable) = {
    "[failed] " +
      (e.toString +: e.getStackTrace.take(30) mkString "\n")
  }
}

object AccessorLoader {
  import scalaz.\/
  import scalaz.\/.{left, right}
  import scalaz.syntax.std.option._

  def inspectSourceAccessor(db: SQLiteDatabase): SourceNotLoaded \/ UnreadSourceAccessor =
    try for {
      accountId <- findCurrentAccountId(db) \/> AccountNotFound
      channelId <- findCurrentChannelId(db, accountId) \/> ChannelNotFound(accountId)
      accessor <- UnreadSourceAccessor.create(db, accountId, channelId) match {
        case Failure(exception) => left(Abort(exception))
        case Success(accessor) => right(accessor)
      }
    } yield accessor catch {
      case e: Exception => left(Abort(e))
    }
}
