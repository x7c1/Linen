package x7c1.linen.repository.channel.my

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{ChannelIdentifiable, ChannelDeletable, ChannelRecord, ChannelStatusRecord}
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.modern.sequence.Sequence

trait MyChannelAccessor extends Sequence[MyChannelRow]{
}

object MyChannelAccessor {
  def createForDebug(db: SQLiteDatabase, accountId: Long): MyChannelAccessor = {
    ClosableMyChannelAccessor.create(db, ClientAccount(accountId)) match {
      case Left(e) => throw e
      case Right(accessor) => accessor
    }
  }
}

trait MyChannelRecord extends TypedFields
  with ChannelRecord
  with ChannelStatusRecord

sealed trait MyChannelRow

case class MyChannel(
  channelId: Long,
  name: String,
  description: String,
  createdAt: Date,
  isSubscribed: Boolean ) extends MyChannelRow

object MyChannel {
  implicit object id extends ChannelIdentifiable[MyChannel]{
    override def toId = _.channelId
  }
  implicit object deletable extends ChannelDeletable[MyChannel](_.channelId)
}

case class MyChannelFooter() extends MyChannelRow
