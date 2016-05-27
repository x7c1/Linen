package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}

trait LoaderScheduleKindRecord extends TypedFields {
  def schedule_kind_id: Long
  def schedule_kind_label: String
  def created_at: Int --> Date
}

object LoaderScheduleKindRecord {
  def table = "loader_schedule_kinds"
  def column = TypedFields.expose[LoaderScheduleKindRecord]

  implicit object id extends ScheduleKindIdentifiable[LoaderScheduleKindRecord]{
    override def toId = _.schedule_kind_id
  }
  implicit object reifiable extends RecordReifiable[LoaderScheduleKindRecord]{
    override def reify(cursor: Cursor) = TypedCursor[LoaderScheduleKindRecord](cursor)
  }
  implicit object findable extends CanFindRecord.Where[ScheduleKindLabelable, LoaderScheduleKindRecord](table){
    override def where[X](id: LoaderScheduleKind) = toArgs(
      column.schedule_kind_label -> id.label
    )
  }
  implicit object providable extends DefaultProvidable[
    ScheduleKindLabelable,
    LoaderScheduleKindRecord
  ]
}

trait ScheduleKindIdentifiable[A] extends Identifiable[A, Long]

trait ScheduleKindLabelable[A] extends Identifiable[A, LoaderScheduleKind]

sealed class LoaderScheduleKind private (val label: String)

object LoaderScheduleKind {
  case object AllChannels extends LoaderScheduleKind("all_channels")
  case object SingleChannel extends LoaderScheduleKind("single_channel")
  case object SingleSource extends LoaderScheduleKind("single_source")

  implicit def labelable[A <: LoaderScheduleKind]: ScheduleKindLabelable[A] = {
    new ScheduleKindLabelable[A] with IdEndo[A]
  }
}