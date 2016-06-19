package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.HasTable
import x7c1.wheat.modern.database.selector.presets.CanFindRecord.Where
import x7c1.wheat.modern.database.selector.presets.DefaultProvidable
import x7c1.wheat.modern.database.selector.{Identifiable, RecordReifiable}

trait LoaderScheduleKindRecord extends TypedFields {
  def schedule_kind_id: Long
  def schedule_kind_label: String --> LoaderScheduleKind
  def created_at: Int --> Date
}

object LoaderScheduleKindRecord {
  def table = "loader_schedule_kinds"

  def column = TypedFields.expose[LoaderScheduleKindRecord]

  implicit object hasTable extends HasTable.Where[LoaderScheduleKindRecord](table)

  implicit object id extends HasScheduleKindId[LoaderScheduleKindRecord]{
    override def toId = _.schedule_kind_id
  }
  implicit object reifiable extends RecordReifiable[LoaderScheduleKindRecord]{
    override def reify(cursor: Cursor) = TypedCursor[LoaderScheduleKindRecord](cursor)
  }
  implicit object findable extends Where[HasScheduleKindLabel, LoaderScheduleKindRecord]{
    override def where[X](id: LoaderScheduleKind) = toArgs(
      column.schedule_kind_label -> id
    )
  }
  implicit object providable extends DefaultProvidable[
    HasScheduleKindLabel,
    LoaderScheduleKindRecord
  ]
}

trait HasScheduleKindId[A] extends Identifiable[A, Long]

trait HasScheduleKindLabel[A] extends Identifiable[A, LoaderScheduleKind]
