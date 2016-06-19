package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{Query, TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.{CanCollectRecord, CanTraverseRecord, CanTraverseRecordByQuery, CollectFrom, TraverseAll, TraverseOn}
import x7c1.wheat.modern.database.selector.{RecordReifiable, SelectorProvidable}
import x7c1.wheat.modern.database.{HasTable, Insertable}
import x7c1.wheat.modern.features.HasShortLength
import x7c1.wheat.modern.sequence.Sequence

trait LoaderScheduleTimeRecord extends TypedFields {
  def schedule_time_id: Long
  def schedule_id: Long
  def start_hour: Int
  def start_minute: Int
  def created_at: Int --> Date
}

object LoaderScheduleTimeRecord {

  def table = "loader_schedule_times"

  def column = TypedFields.expose[LoaderScheduleTimeRecord]

  implicit object hasTable extends HasTable.Where[LoaderScheduleTimeRecord](table)

  implicit object reifiable extends RecordReifiable[LoaderScheduleTimeRecord]{
    override def reify(cursor: Cursor) = TypedCursor[LoaderScheduleTimeRecord](cursor)
  }
  implicit object traverseAll extends CanTraverseRecordByQuery[LoaderScheduleTimeRecord](
    Query("SELECT * FROM loader_schedule_times")
  )
  implicit object traverseOn extends CanTraverseRecord[HasAccountId, LoaderScheduleTimeRecord]{
    override def queryAbout[X: HasAccountId](target: X) = {
      val sql =
        """SELECT *
          | FROM loader_schedule_times AS t1
          | LEFT JOIN loader_schedules AS t2
          |  ON t1.schedule_id = t2.schedule_id
          | WHERE t2.account_id = ?
          |""".stripMargin

      val accountId = implicitly[HasAccountId[X]] toId target
      Query(sql, Array(accountId.toString))
    }
  }
  implicit object providable extends SelectorProvidable[LoaderScheduleTimeRecord, Selector]

  implicit object collect
    extends CanCollectRecord.Where[HasLoaderScheduleId, LoaderScheduleTimeRecord]{

    override def where[X](id: Long) = toArgs(
      column.schedule_id -> id
    )
  }
  implicit object scheduleId extends HasLoaderScheduleId[LoaderScheduleTimeRecord]{
    override def toId = _.schedule_id
  }
  implicit class RichSequence(xs: Sequence[LoaderScheduleTimeRecord]){
    implicit object short extends HasShortLength[LoaderScheduleTimeRecord]

    def groupByScheduleId: Map[Long, Seq[LoaderScheduleTimeRecord]] = {
      xs.toSeq.groupBy(_.schedule_id)
    }
  }
  class Selector(protected val db: SQLiteDatabase)
    extends CollectFrom[HasLoaderScheduleId, LoaderScheduleTimeRecord]
      with TraverseAll[LoaderScheduleTimeRecord]
      with TraverseOn[HasAccountId, LoaderScheduleTimeRecord]
}

case class ScheduleTimeParts(
  scheduleId: Long,
  startHour: Int,
  startMinute: Int,
  createdAt: Date
)

object ScheduleTimeParts {
  import LoaderScheduleTimeRecord.column
  implicit object insertable extends Insertable[ScheduleTimeParts]{
    override def tableName: String = LoaderScheduleTimeRecord.table
    override def toContentValues(target: ScheduleTimeParts): ContentValues =
      TypedFields.toContentValues(
        column.schedule_id -> target.scheduleId,
        column.start_hour -> target.startHour,
        column.start_minute -> target.startMinute,
        column.created_at -> target.createdAt
      )
  }
}
