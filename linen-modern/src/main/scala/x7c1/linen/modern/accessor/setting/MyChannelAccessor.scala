package x7c1.linen.modern.accessor.setting

import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView.ViewHolder
import x7c1.linen.modern.accessor.database.{ChannelRecord, ChannelStatusRecord}
import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.macros.logger.Log

trait MyChannelAccessor {
  def accountId: Long
  def findAt(position: Int): Option[SettingMyChannelRow]
  def length: Int
  def bindViewHolder[A <: ViewHolder]
    (holder: A, position: Int)
    (block: PartialFunction[(A, SettingMyChannelRow), Unit]) = {

    findAt(position) -> holder match {
      case (Some(item), _) if block isDefinedAt (holder, item) =>
        block(holder, item)
      case (item, _) =>
        Log error s"unknown item:$item, holder:$holder"
    }
  }
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
