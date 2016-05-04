package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{Identifiable, RecordReifiable}
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
    override def where[X](key: ChannelStatusKey) = Seq(
      "account_id" -> key.accountId.toString,
      "channel_id" -> key.channelId.toString
    )
  }
}

case class ChannelStatusKey (
  channelId: Long,
  accountId: Long
)
object ChannelStatusKey {
  implicit object id extends ChannelStatusIdentifiable[ChannelStatusKey]{
    override def idOf(target: ChannelStatusKey): ChannelStatusKey = target
  }
}

trait ChannelStatusIdentifiable[A] extends Identifiable[A, ChannelStatusKey]

case class ChannelStatusRecordParts(
  channelId: Long,
  accountId: Long,
  subscribed: Boolean
)
object ChannelStatusRecordParts {
  implicit object updatable extends Updatable[ChannelStatusRecordParts]{
    override def tableName = ChannelStatusRecord.table
    override def toContentValues(target: ChannelStatusRecordParts) = {
      val column = TypedFields.expose[ChannelStatusRecord]
      TypedFields toContentValues (
        column.account_id -> target.accountId,
        column.channel_id -> target.channelId,
        column.subscribed -> (if (target.subscribed) 1 else 0)
      )
    }
    override def where(target: ChannelStatusRecordParts) = Seq(
      "channel_id" -> target.channelId.toString,
      "account_id" -> target.accountId.toString
    )
  }
  implicit object insertable extends Insertable[ChannelStatusRecordParts]{
    override def tableName = ChannelStatusRecord.table
    override def toContentValues(target: ChannelStatusRecordParts) = {
      val column = TypedFields.expose[ChannelStatusRecord]
      TypedFields toContentValues (
        column.account_id -> target.accountId,
        column.channel_id -> target.channelId,
        column.subscribed -> (if (target.subscribed) 1 else 0),
        column.created_at -> Date.current()
      )
    }
  }
}


