package x7c1.linen.repository.channel.order

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.ChannelStatusRecord.column
import x7c1.linen.database.struct.{ChannelStatusKey, ChannelStatusRecord, HasAccountId}
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.modern.database.selector.presets.CanCollectRecord.Where
import x7c1.wheat.modern.database.selector.presets.{CanCollect, CollectFrom}
import x7c1.wheat.modern.database.selector.{CursorConvertible, SelectorProvidable}

case class OrderedChannel(
  channelId: Long,
  accountId: Long,
  channelRank: Double
)

object OrderedChannel {
  implicit object providable extends SelectorProvidable[OrderedChannel, OrderedChannelSelector]

  implicit object collect extends CanCollectOrdered

  implicit object rank extends HasChannelRank[OrderedChannel]{
    override def rankOf(x: OrderedChannel) = x.channelRank
    override def toId = channel =>
      ChannelStatusKey(
        channelId = channel.channelId,
        accountId = channel.accountId
      )
  }
}

class OrderedChannelSelector(
  override protected val db: SQLiteDatabase) extends CollectFrom[HasAccountId, OrderedChannel]{
}

private[order] class CanCollectOrdered extends CanCollect[HasAccountId, OrderedChannel]{
  override def extract[X: HasAccountId](db: SQLiteDatabase, id: X) = {
    traverser.extract(db, id).right map {
      _ sortBy (_.channel_rank) map readable.convertFrom
    }
  }
  private object traverser extends Where[HasAccountId, ChannelStatusRecord](ChannelStatusRecord.table){
    override def where[X](id: Long) = toArgs(
      column.account_id -> id
    )
  }
  private object readable extends CursorConvertible[ChannelStatusRecord, OrderedChannel]{
    override def convertFrom = cursor =>
      OrderedChannel(
        channelId = cursor.channel_id,
        accountId = cursor.account_id,
        channelRank = cursor.channel_rank
      )
  }
}
