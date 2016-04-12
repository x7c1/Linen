package x7c1.linen.modern.accessor.setting

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.{ChannelRecord, ChannelStatusRecord}
import x7c1.linen.domain.Date
import x7c1.linen.domain.account.ClientAccount
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
  isSubscribed: Boolean
) extends MyChannelRow

case class MyChannelFooter() extends MyChannelRow
