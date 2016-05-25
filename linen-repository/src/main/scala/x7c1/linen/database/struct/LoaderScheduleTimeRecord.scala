package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.RecordReifiable
import x7c1.wheat.modern.database.selector.presets.{CanCollectRecord, CanTraverseRecordByQuery, DefaultProvidable}

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
  implicit object providable
    extends DefaultProvidable[LoaderScheduleLike, LoaderScheduleTimeRecord]

  implicit object collect
    extends CanCollectRecord.Where[LoaderScheduleLike, LoaderScheduleTimeRecord](table){

    override def where[X](id: Long) = toArgs(
      columns.schedule_id -> id
    )
  }
  implicit object likeSchedule extends LoaderScheduleLike[LoaderScheduleTimeRecord]{
    override def toId = _.schedule_id
  }
}
