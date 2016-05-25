package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.presets.{CanTraverseRecordByQuery, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}

trait LoaderScheduleRecord extends TypedFields {
  def schedule_id: Long
  def account_id: Long
  def schedule_kind_id: Long
  def enabled: Int
}

object LoaderScheduleRecord {

  def table = "loader_schedules"

  def column = TypedFields.expose[LoaderScheduleRecord]

  implicit object providable
    extends DefaultProvidable[AccountIdentifiable, LoaderScheduleRecord]

  implicit object reifiable extends RecordReifiable[LoaderScheduleRecord]{
    override def reify(cursor: Cursor) = TypedCursor[LoaderScheduleRecord](cursor)
  }
  implicit object traverseAll
    extends CanTraverseRecordByQuery[LoaderScheduleRecord](
      Query("SELECT * FROM loader_schedules")
    )
}

trait LoaderScheduleLike[A] extends Identifiable[A, Long]

object LoaderScheduleLike {
  implicit object id extends LoaderScheduleLike[Long] with IdEndo[Long]
}

trait LoaderScheduleKindRecord extends TypedFields {
  def schedule_kind_id: Long
  def schedule_kind_label: String
  def created_at: Int --> Date
}

sealed class LoaderScheduleKind private (val label: String)

object LoaderScheduleKind {
  case object AllChannels extends LoaderScheduleKind("all_channels")
  case object SingleChannel extends LoaderScheduleKind("single_channel")
  case object SingleSource extends LoaderScheduleKind("single_source")
}
