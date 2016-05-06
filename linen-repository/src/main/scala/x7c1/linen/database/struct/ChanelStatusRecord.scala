package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toSelectionArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}
import x7c1.wheat.modern.database.{Insertable, Updatable}


trait ChannelStatusRecord extends TypedFields {
  def channel_id: Long
  def account_id: Long
  def subscribed: Int
  def created_at: Int --> Date
}

object ChannelStatusRecord {
  def table: String = "channel_statuses"
  def column = TypedFields.expose[ChannelStatusRecord]

  implicit object providable
    extends DefaultProvidable[ChannelStatusIdentifiable, ChannelStatusRecord]

  implicit object reifiable extends RecordReifiable[ChannelStatusRecord]{
    override def reify(cursor: Cursor) = TypedCursor[ChannelStatusRecord](cursor)
  }
  implicit object findable extends CanFindRecord.Where[ChannelStatusIdentifiable, ChannelStatusRecord](table){
    override def where[X](key: ChannelStatusKey) = toSelectionArgs(
      column.account_id -> key.accountId,
      column.channel_id -> key.channelId
    )
  }
}

case class ChannelStatusKey (
  channelId: Long,
  accountId: Long
)
object ChannelStatusKey {
  implicit object id
    extends ChannelStatusIdentifiable[ChannelStatusKey]
      with IdEndo[ChannelStatusKey]
}

trait ChannelStatusIdentifiable[A] extends Identifiable[A, ChannelStatusKey]

case class ChannelStatusRecordParts(
  channelId: Long,
  accountId: Long,
  subscribed: Boolean
)
object ChannelStatusRecordParts {
  import ChannelStatusRecord.column

  implicit object updatable extends Updatable[ChannelStatusRecordParts]{
    override def tableName = ChannelStatusRecord.table
    override def toContentValues(target: ChannelStatusRecordParts) = {
      TypedFields toContentValues (
        column.account_id -> target.accountId,
        column.channel_id -> target.channelId,
        column.subscribed -> (if (target.subscribed) 1 else 0)
      )
    }
    override def where(target: ChannelStatusRecordParts) = toSelectionArgs(
      column.channel_id -> target.channelId,
      column.account_id -> target.accountId
    )
  }
  implicit object insertable extends Insertable[ChannelStatusRecordParts]{
    override def tableName = ChannelStatusRecord.table
    override def toContentValues(target: ChannelStatusRecordParts) = {
      TypedFields toContentValues (
        column.account_id -> target.accountId,
        column.channel_id -> target.channelId,
        column.subscribed -> (if (target.subscribed) 1 else 0),
        column.created_at -> Date.current()
      )
    }
  }
}


