package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.HasTable
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{Identifiable, RecordReifiable}

trait NotificationIdRecord extends TypedFields {
  def notification_id: Int
  def notification_content_key: String
  def notification_content_kind: String --> NotificationContentKind
  def created_at: Int --> Date
}

object NotificationIdRecord {
  def table = "notification_ids"

  def column = TypedFields.expose[NotificationIdRecord]

  implicit object hasTable extends HasTable.Where[NotificationIdRecord](table)

  implicit object reifiable extends RecordReifiable[NotificationIdRecord]{
    override def reify(cursor: Cursor) = TypedCursor[NotificationIdRecord](cursor)
  }
  implicit object findable extends CanFindRecord.Where[HasNotificationKey, NotificationIdRecord]{
    override def where[X](key: NotificationKey) = toArgs(
      column.notification_content_key -> key.contentKey,
      column.notification_content_kind -> key.contentKind
    )
  }
  implicit object providable extends DefaultProvidable[HasNotificationKey, NotificationIdRecord]
}

trait HasNotificationKey[A] extends Identifiable[A, NotificationKey]
