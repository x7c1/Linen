package x7c1.linen.database.struct

import android.database.{Cursor, SQLException}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.account.AccountBase
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.{Insertable, SingleWhere, Updatable, WritableDatabase}
import x7c1.wheat.modern.either.OptionEither


trait ChannelStatusRecord extends TypedFields {
  def channel_id: Long
  def account_id: Long
  def subscribed: Int
  def created_at: Int --> Date
}

object ChannelStatusRecord {
  def table: String = "channel_statuses"
  def column = TypedFields.expose[ChannelStatusRecord]

  implicit def selectable[A <: AccountBase]: SingleWhere[ChannelStatusRecord, (A, Long)] =
    new SingleWhere[ChannelStatusRecord, (A, Long)](table){
      override def where(id: (A, Long)): Seq[(String, String)] = id match {
        case (account, channelId) => Seq(
          "account_id" -> account.accountId.toString,
          "channel_id" -> channelId.toString
        )
      }
      override def fromCursor(cursor: Cursor) = {
        TypedCursor[ChannelStatusRecord](cursor).freezeAt(0)
      }
    }
}

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


