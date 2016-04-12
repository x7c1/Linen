package x7c1.linen.database

import android.database.{Cursor, SQLException}
import x7c1.linen.domain.Date
import x7c1.linen.domain.account.AccountIdentifiable
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.macros.logger.Log


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
    Writer(subscribed = true) insertOrUpdate channelId
  }
  def unsubscribe(channelId: Long): Either[SQLException, Unit] = {
    Writer(subscribed = false) insertOrUpdate channelId
  }
  private case class Writer(subscribed: Boolean){
    def insertOrUpdate(channelId: Long) = {
      helper.readable.find[ChannelStatusRecord] by (account -> channelId) match {
        case Right(Some(record)) => update(channelId)
        case Right(None) => insert(channelId)
        case Left(error) => Left(error)
      }
    }
    private def execute[A, B](channelId: Long)
      (f: (WritableDatabase, ChannelStatusRecordParts) => Either[A, B]) = {

      val either = WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
        f(writable, ChannelStatusRecordParts(
          channelId = channelId,
          accountId = account.accountId,
          subscribed = subscribed
        ))
      }
      either.right map (_ => {})
    }
    private def update(channelId: Long) = {
      Log info s"subscribed($subscribed), channel($channelId)"
      execute(channelId){_ update _}
    }
    private def insert(channelId: Long) = {
      Log info s"subscribed($subscribed), channel($channelId)"
      execute(channelId){_ insert _}
    }
  }
}
