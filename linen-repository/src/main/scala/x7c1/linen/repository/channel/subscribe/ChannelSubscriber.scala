package x7c1.linen.repository.channel.subscribe

import android.database.SQLException
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{ChannelStatusKey, ChannelStatusRecord, ChannelStatusRecordParts, HasAccountId}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.WritableDatabase
import x7c1.wheat.modern.either.OptionEither


class ChannelSubscriber[X: HasAccountId](account: X, helper: DatabaseHelper) {
  import x7c1.wheat.modern.either.Imports._

  def subscribe(channelId: Long): OptionEither[SQLException, Unit] = {
    Writer(subscribed = true) insertOrUpdate channelId
  }
  def unsubscribe(channelId: Long): OptionEither[SQLException, Unit] = {
    Writer(subscribed = false) insertOrUpdate channelId
  }
  private case class Writer(subscribed: Boolean){
    def insertOrUpdate(channelId: Long) = {
      val either = helper.selectorOf[ChannelStatusRecord] findBy ChannelStatusKey(
        channelId = channelId,
        accountId = implicitly[HasAccountId[X]] toId account
      )
      either.option flatMap {
        case Some(record) => update(channelId).toOptionEither
        case None => insert(channelId).toOptionEither
      }
    }
    private def execute[A, B]
      (channelId: Long)
      (f: (WritableDatabase, ChannelStatusRecordParts) => Either[A, B]) = {

      val either = WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
        f(writable, ChannelStatusRecordParts(
          channelId = channelId,
          accountId = implicitly[HasAccountId[X]] toId account,
          channelRank = 0,
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
