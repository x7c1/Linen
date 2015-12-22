package x7c1.linen.modern.init

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.pattern.after
import akka.util.Timeout
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.{EntryAccessor, EntryAccessorBinder, SourceAccessor}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline, Source}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

import scala.concurrent.duration.DurationInt
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, SyncVar}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

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
    Timeout(5.seconds)
  }

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

  def startLoading() = {
    val first = for {
      sourceIds <- (actor ? StartLoading).mapTo[Seq[Long]]
      remaining <- loadSources(sourceIds)
      _ <- actor ? NotifyChanged
    } yield {
      remaining
    }
    first flatMap loadMore onComplete {
      case Success(ids) =>
        Log info "[done] " + ids
      case Failure(e) =>
        Log error "[failed] " +
          (e.getMessage +: e.getStackTrace.take(30) mkString "\n")
    }
  }
  private def loadSources(sourceIds: Seq[Long]) = {
    (actor ? LoadSources(sourceIds)).mapTo[Seq[Long]]
  }
  private def loadMore(remaining: Seq[Long]): Future[Seq[Long]] = {
    Log info s"[init] remaining(${remaining.length})"
    remaining match {
      case Seq() => Future successful Seq()
      case _ => for {
        ids <- loadSources(remaining)
        remaining <- after(50.milliseconds, system.scheduler)(loadMore(ids))
      } yield remaining
    }
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
  private def loadSources(remainingSourceIds: Seq[Long]) = {
    val (sourceIds, remains) = remainingSourceIds splitAt 50
    val current = currentSourceLength.take()
    currentSourceLength put (current + sourceIds.length)

    val positions = EntryAccessor.createPositionMap(database, sourceIds)
    val outlines = EntryAccessor.forEntryOutline(database, sourceIds, positions)
    outlineAccessors += outlines

    val details = EntryAccessor.forEntryDetail(database, sourceIds, positions)
    detailAccessors += details

    sender() ! remains
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
