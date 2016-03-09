package x7c1.linen.modern.accessor.database

import android.database.{SQLException, Cursor}
import x7c1.linen.modern.accessor.{AccountIdentifiable, Insertable, LinenOpenHelper, SingleWhere, Updatable, WritableDatabase}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}


trait ChannelStatusRecord extends TypedFields {
  def channel_id: Long
  def account_id: Long
  def subscribed: Int
  def created_at: Int --> Date
}

object ChannelStatusRecord {
  def table: String = "channel_statuses"
  def column = TypedFields.expose[ChannelStatusRecord]

  implicit def selectable[A <: AccountIdentifiable]: SingleWhere[ChannelStatusRecord, (A, Long)] =
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

class ChannelSubscriber(account: AccountIdentifiable, helper: LinenOpenHelper){
  def subscribe(channelId: Long): Either[SQLException, Unit] = {
    helper.readable.find[ChannelStatusRecord] by (account -> channelId) match {
      case Right(Some(record)) => update(channelId)
      case Right(None) => insert(channelId)
      case Left(error) => Left(error)
    }
  }
  private def update(channelId: Long) = {
    val either = WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
      writable update ChannelStatusRecordParts(
        channelId = channelId,
        accountId = account.accountId,
        subscribed = true
      )
    }
    either.right map (_ => {})
  }
  private def insert(channelId: Long) = {
    val either = WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
      writable insert ChannelStatusRecordParts(
        channelId = channelId,
        accountId = account.accountId,
        subscribed = true
      )
    }
    either.right map (_ => {})
  }
}
