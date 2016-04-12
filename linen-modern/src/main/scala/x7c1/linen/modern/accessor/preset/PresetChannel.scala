package x7c1.linen.modern.accessor.preset

import android.database.Cursor
import x7c1.linen.database.{ChannelRecord, SingleWhere}
import x7c1.linen.modern.struct.Channel
import x7c1.wheat.macros.database.TypedCursor

case class PresetChannel(
  channelId: Long,
  accountId: Long,
  name: String
)
object PresetChannel {
  implicit object selectable
    extends SingleWhere[PresetChannel, (PresetAccount, PresetChannelPiece)](Channel.table){

    override def where(id: (PresetAccount, PresetChannelPiece)) = id match {
      case (account, channel) => Seq(
        "account_id" -> account.accountId.toString,
        "name" -> channel.name
      )
    }
    override def fromCursor(cursor: Cursor) = {
      TypedCursor[ChannelRecord](cursor) moveToHead reify
    }
    private def reify(record: ChannelRecord) =
      PresetChannel(
        channelId = record._id,
        accountId = record.account_id,
        name = record.name
      )
  }
}

case class PresetChannelPiece(
  name: String,
  description: String
)
