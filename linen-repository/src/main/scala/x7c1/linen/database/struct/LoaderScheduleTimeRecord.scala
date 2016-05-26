package x7c1.linen.database.struct

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.presets.{CanCollectRecord, CanTraverseRecord, CanTraverseRecordByQuery, CollectFrom, TraverseAll, TraverseOn}
import x7c1.wheat.modern.database.selector.{RecordReifiable, SelectorProvidable}
import x7c1.wheat.modern.features.HasShortLength
import x7c1.wheat.modern.sequence.Sequence

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait LoaderScheduleTimeRecord extends TypedFields {
  def schedule_time_id: Long
  def schedule_id: Long
  def start_hour: Int
  def start_minute: Int
  def created_at: Int --> Date
}

object LoaderScheduleTimeRecord {

  def table = "loader_schedule_times"

  def columns = TypedFields.expose[LoaderScheduleTimeRecord]

  implicit object reifiable extends RecordReifiable[LoaderScheduleTimeRecord]{
    override def reify(cursor: Cursor) = TypedCursor[LoaderScheduleTimeRecord](cursor)
  }
  implicit object traverseAll extends CanTraverseRecordByQuery[LoaderScheduleTimeRecord](
    Query("SELECT * FROM loader_schedule_times")
  )
  implicit object traverseOn extends CanTraverseRecord[AccountIdentifiable, LoaderScheduleTimeRecord]{
    override def query[X: AccountIdentifiable](target: X) = {
      val sql =
        """SELECT * FROM loader_schedule_times as t1
          | LEFT JOIN loader_schedules as t2 ON t1.schedule_id = t2.schedule_id
          | WHERE t2.account_id = ?
          | """.stripMargin

      val accountId = implicitly[AccountIdentifiable[X]] toId target
      Query(sql, Array(accountId.toString))
    }
  }
  implicit object providable
    extends SelectorProvidable[LoaderScheduleTimeRecord, Selector](new Selector(_))

  implicit object collect
    extends CanCollectRecord.Where[LoaderScheduleLike, LoaderScheduleTimeRecord](table){

    override def where[X](id: Long) = toArgs(
      columns.schedule_id -> id
    )
  }
  implicit object scheduleId extends LoaderScheduleLike[LoaderScheduleTimeRecord]{
    override def toId = _.schedule_id
  }
  implicit class RichSequence(xs: Sequence[LoaderScheduleTimeRecord]){
    implicit object short extends HasShortLength[LoaderScheduleTimeRecord]

    def groupByScheduleId: Map[Long, Seq[LoaderScheduleTimeRecord]] = {
      val map = mutable.Map[Long, ListBuffer[LoaderScheduleTimeRecord]]()
      xs.toSeq.foreach { time =>
        val buffer = map.getOrElseUpdate(time.schedule_id, ListBuffer())
        buffer += time
      }
      map.toMap
    }
  }
  class Selector(protected val db: SQLiteDatabase)
    extends CollectFrom[LoaderScheduleLike, LoaderScheduleTimeRecord]
      with TraverseAll[LoaderScheduleTimeRecord]
      with TraverseOn[AccountIdentifiable, LoaderScheduleTimeRecord]
}
