package x7c1.linen.modern.init

import akka.actor.{Actor, ActorSystem, Cancellable, Props}
import akka.pattern.ask
import akka.util.Timeout
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.{EntryAccessor, EntryAccessorBinder, SourceAccessor}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline, Source}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.SyncVar
import scala.concurrent.duration.DurationInt
import scala.util.{Try, Failure, Success}

class AccessorLoader(database: SQLiteDatabase, layout: MainLayout){

  private lazy val sourceAccessor = {
    val x = new SyncVar[Option[SourceAccessor]]
    x put None
    x
  }
  private lazy val currentSourceLength = {
    val x = new SyncVar[Int]
    x put 0
    x
  }
  private val outlineAccessors = ListBuffer[EntryAccessor[EntryOutline]]()

  private val detailAccessors = ListBuffer[EntryAccessor[EntryDetail]]()

  private lazy val system = ActorSystem("accessor-loader-system")

  private lazy val actor = system actorOf Props(
    classOf[AccessorLoaderActor],
    database,
    layout,
    sourceAccessor,
    currentSourceLength,
    outlineAccessors,
    detailAccessors
  )
  private implicit val timeout = {
    import scala.concurrent.duration.DurationInt
    Timeout(3.seconds)
  }
  private var currentSchedule: Option[Cancellable] = None

  def createSourceAccessor: SourceAccessor =
    new SourceAccessor {
      override def findAt(position: Int): Option[Source] = {
        sourceAccessor.get.flatMap(_ findAt position)
      }
      override def positionOf(sourceId: Long): Option[Int] = {
        sourceAccessor.get.flatMap(_ positionOf sourceId)
      }
      override def length: Int = currentSourceLength.get
    }

  def createOutlineAccessor: EntryAccessor[EntryOutline] =
    new EntryAccessorBinder(outlineAccessors)

  def createDetailAccessor: EntryAccessor[EntryDetail] =
    new EntryAccessorBinder(detailAccessors)

  def startLoading(): Unit = {
    val first = for {
      sourceIds <- (actor ? StartLoading).mapTo[Seq[Long]]
      remaining <- loadSources(sourceIds)
      _ <- actor ? NotifyChanged
    } yield {
      remaining
    }
    first onComplete loadNext
  }
  def close(): Unit = {
    currentSchedule foreach { _.cancel() }
    system stop actor
  }
  private def loadSources(sourceIds: Seq[Long]) = {
    (actor ? LoadSources(sourceIds)).mapTo[Either[Throwable, Seq[Long]]]
  }
  private def loadMore(remaining: Seq[Long]): Unit = {
    Log info s"[init] remaining(${remaining.length})"
    remaining match {
      case Seq() => Log info "[done]"
      case _ =>
        currentSchedule = Some apply system.scheduler.scheduleOnce(50.milliseconds){
          loadSources(remaining) onComplete loadNext
        }
    }
  }
  private def loadNext: Try[Either[Throwable, Seq[Long]]] => Unit = {
    case Success(Right(ids)) => loadMore(ids)
    case Success(Left(e : IllegalStateException)) =>

      /*
      java.lang.IllegalStateException:
        attempt to re-open an already-closed object:
          SQLiteDatabase: /data/user/0/x7c1.linen/databases/linen-db

      exception above is thrown
        after close() is called when actor#loadSources remains proceeding
      */
      Log warn e.toString

    case Success(Left(e)) => Log error formatError(e)
    case Failure(e) => Log error formatError(e)
  }
  private def formatError(e: Throwable) = {
    "[failed] " +
      (e.toString +: e.getStackTrace.take(30) mkString "\n")
  }

}

class AccessorLoaderActor(
  database: SQLiteDatabase,
  layout: MainLayout,
  sourceAccessor: SyncVar[Option[SourceAccessor]],
  currentSourceLength: SyncVar[Int],
  outlineAccessors: ListBuffer[EntryAccessor[EntryOutline]],
  detailAccessors: ListBuffer[EntryAccessor[EntryDetail]] ) extends Actor {

  override def receive = {
    case StartLoading => startLoading()
    case LoadSources(remainingSourceIds) => loadSources(remainingSourceIds)
    case NotifyChanged => sender() ! notifyChanged()
  }
  private def startLoading() = {
    val accessor = SourceAccessor create database
    sourceAccessor.take()
    sourceAccessor put Some(accessor)

    val sourceIds = (0 to accessor.length - 1).
      map(accessor.findAt).flatMap(_.map(_.id))

    sender() ! sourceIds
  }
  private def loadSources(remainingSourceIds: Seq[Long]) = try {
    val (sourceIds, remains) = remainingSourceIds splitAt 50
    val current = currentSourceLength.take()
    currentSourceLength put (current + sourceIds.length)

    val positions = EntryAccessor.createPositionMap(database, sourceIds)
    val outlines = EntryAccessor.forEntryOutline(database, sourceIds, positions)
    outlineAccessors += outlines

    val details = EntryAccessor.forEntryDetail(database, sourceIds, positions)
    detailAccessors += details

    sender() ! Right(remains)
  } catch {
    case e: Throwable => sender() ! Left(e)
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
}

case object StartLoading
case class LoadSources(sourceIds: Seq[Long])
case object NotifyChanged
