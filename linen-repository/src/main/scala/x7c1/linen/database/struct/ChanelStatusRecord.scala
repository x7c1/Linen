package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}
import x7c1.wheat.modern.database.{Insertable, Updatable}


trait ChannelStatusRecord extends TypedFields {
  def channel_id: Long
  def account_id: Long
  def channel_rank: Double
  def subscribed: Int --> Boolean
  def created_at: Int --> Date
  def updated_at: Int --> Date
}

object ChannelStatusRecord {
  def table: String = "channel_statuses"
  def column = TypedFields.expose[ChannelStatusRecord]

  implicit object providable
    extends DefaultProvidable[HasChannelStatusKey, ChannelStatusRecord]

  implicit object reifiable extends RecordReifiable[ChannelStatusRecord]{
    override def reify(cursor: Cursor) = TypedCursor[ChannelStatusRecord](cursor)
  }
  implicit object findable extends CanFindRecord.Where[HasChannelStatusKey, ChannelStatusRecord](table){
    override def where[X](key: ChannelStatusKey) = toArgs(
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
    extends HasChannelStatusKey[ChannelStatusKey]
      with IdEndo[ChannelStatusKey]
}

trait HasChannelStatusKey[A] extends Identifiable[A, ChannelStatusKey]

case class ChannelStatusRecordParts(
  channelId: Long,
  accountId: Long,
  channelRank: Double,
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
        column.channel_rank -> target.channelRank,
        column.updated_at -> Date.current(),
        column.subscribed -> target.subscribed
      )
    }
    override def where(target: ChannelStatusRecordParts) = toArgs(
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
        column.channel_rank -> target.channelRank,
        column.subscribed -> target.subscribed,
        column.updated_at -> Date.current(),
        column.created_at -> Date.current()
      )
    }
  }
}

case class ChannelRankParts(
  accountId: Long,
  channelId: Long,
  channelRank: Double
)
object ChannelRankParts {
  import ChannelStatusRecord.column

  implicit object updatable extends Updatable[ChannelRankParts]{
    override def tableName = ChannelStatusRecord.table
    override def toContentValues(target: ChannelRankParts) = {
      TypedFields toContentValues (
        column.account_id -> target.accountId,
        column.channel_id -> target.channelId,
        column.channel_rank -> target.channelRank,
        column.updated_at -> Date.current()
      )
    }
    override def where(target: ChannelRankParts) = toArgs(
      column.channel_id -> target.channelId,
      column.account_id -> target.accountId
    )
  }

  def apply[A: HasChannelStatusKey](origin: A, channelRank: Double): ChannelRankParts = {
    val key = implicitly[HasChannelStatusKey[A]] toId origin
    new ChannelRankParts(
      accountId = key.accountId,
      channelId = key.channelId,
      channelRank = channelRank
    )
  }
}
