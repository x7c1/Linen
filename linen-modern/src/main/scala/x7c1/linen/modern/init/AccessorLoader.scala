package x7c1.linen.modern.init

import android.app.LoaderManager
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.{EntryAccessor, EntryAccessorBinder, SourceAccessor}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline, Source}
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
  private val factory = new FiniteLoaderFactory(
    context = layout.itemView.context,
    loaderManager = loaderManager,
    startLoaderId = 0
  )
  private var sourceAccessor: Option[SourceAccessor] = None
  private var currentSourceLength: Int = 0

  def createSourceAccessor: SourceAccessor =
    new SourceAccessor {
      override def findAt(position: Int): Option[Source] = {
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
    factory.close()

    synchronized {
      currentSourceLength = 0
      sourceAccessor = None
      outlineAccessors.clear()
      detailAccessors.clear()
    }
  }
  private def startLoadingSources() = factory asFuture {
    val accessor = SourceAccessor create database
    sourceAccessor = Some(accessor)

    val sourceIds = (0 to accessor.length - 1).
      map(accessor.findAt).flatMap(_.map(_.id))

    sourceIds
  }
  private def loadSourceEntries(remainingSourceIds: Seq[Long]) = factory asFuture {
    val (sourceIds, remains) = remainingSourceIds splitAt 50
    currentSourceLength += sourceIds.length

    val positions = EntryAccessor.createPositionMap(database, sourceIds)
    val outlines = EntryAccessor.forEntryOutline(database, sourceIds, positions)
    outlineAccessors += outlines

    val details = EntryAccessor.forEntryDetail(database, sourceIds, positions)
    detailAccessors += details

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
