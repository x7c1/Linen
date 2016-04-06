package x7c1.linen.modern.accessor.setting

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.accessor.database.{ChannelRecord, ChannelStatusRecord}
import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.modern.sequence.Sequence

trait MyChannelAccessor extends Sequence[SettingMyChannelRow]{
  def accountId: Long
}

object MyChannelAccessor {
  def createForDebug(db: SQLiteDatabase, accountId: Long): MyChannelAccessor = {
    InternalMyChannelAccessor.create(db, ClientAccount(accountId)) match {
      case Left(e) => throw e
      case Right(accessor) => accessor
    }
  }
}

trait MyChannelRecord extends TypedFields
  with ChannelRecord
  with ChannelStatusRecord

sealed trait SettingMyChannelRow

case class SettingMyChannel(
  channelId: Long,
  name: String,
  description: String,
  createdAt: Date,
  isSubscribed: Boolean
) extends SettingMyChannelRow

case class SettingMyChannelFooter() extends SettingMyChannelRow
