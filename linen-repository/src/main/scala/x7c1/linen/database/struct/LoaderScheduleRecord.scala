package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{Query, TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{Insertable, Updatable}
import x7c1.wheat.modern.database.selector.presets.{CanTraverseRecordByQuery, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}

trait LoaderScheduleRecord extends TypedFields {
  def schedule_id: Long
  def account_id: Long
  def schedule_kind_id: Long
  def enabled: Int
  def created_at: Int --> Date
}

object LoaderScheduleRecord {

  def table = "loader_schedules"

  def column = TypedFields.expose[LoaderScheduleRecord]

  implicit object providable
    extends DefaultProvidable[HasAccountId, LoaderScheduleRecord]

  implicit object reifiable extends RecordReifiable[LoaderScheduleRecord]{
    override def reify(cursor: Cursor) = TypedCursor[LoaderScheduleRecord](cursor)
  }
  implicit object traverseAll
    extends CanTraverseRecordByQuery[LoaderScheduleRecord](
      Query("SELECT * FROM loader_schedules")
    )
}

case class LoaderScheduleParts(
  accountId: Long,
  kindId: Long,
  enabled: Boolean,
  createdAt: Date
)

object LoaderScheduleParts {
  import LoaderScheduleRecord.column

  implicit object insertable extends Insertable[LoaderScheduleParts]{
    override def tableName: String = LoaderScheduleRecord.table
    override def toContentValues(target: LoaderScheduleParts): ContentValues = {
      TypedFields.toContentValues(
        column.account_id -> target.accountId,
        column.schedule_kind_id -> target.kindId,
        column.enabled -> (if (target.enabled) 1 else 0),
        column.created_at -> target.createdAt
      )
    }
  }
  case class ToChangeState(
    scheduleId: Long,
    enabled: Boolean
  )
  object ToChangeState {
    implicit object updatable extends Updatable[ToChangeState]{
      override def tableName = LoaderScheduleRecord.table
      override def toContentValues(target: ToChangeState) = TypedFields toContentValues (
        column.enabled -> (if (target.enabled) 1 else 0)
      )
      override def where(target: ToChangeState) = toArgs(
        column.schedule_id -> target.scheduleId
      )
    }
  }
}

trait HasLoaderScheduleId[A] extends Identifiable[A, Long]

object HasLoaderScheduleId {
  implicit object id extends HasLoaderScheduleId[Long] with IdEndo[Long]
}
