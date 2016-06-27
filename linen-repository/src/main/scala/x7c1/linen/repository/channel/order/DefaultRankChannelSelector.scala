package x7c1.linen.repository.channel.order

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.ChannelStatusRecord.column
import x7c1.linen.database.struct.{ChannelStatusRecord, HasAccountId}
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.modern.database.selector.CursorConvertible
import x7c1.wheat.modern.database.selector.presets.CanCollectRecord.Where
import x7c1.wheat.modern.database.selector.presets.{CanCollect, CollectFrom}

class DefaultRankChannelSelector(
  protected val db: SQLiteDatabase) extends CollectFrom[HasAccountId, DefaultRankChannel]

private[order] class CanCollectImpl extends CanCollect[HasAccountId, DefaultRankChannel]{
  private object collector extends Where[HasAccountId, ChannelStatusRecord]{
    override def where[X](id: Long) = toArgs(
      column.account_id -> id,
      column.channel_rank -> 0D
    )
  }
  private object readable extends CursorConvertible[ChannelStatusRecord, DefaultRankChannel]{
    override def convertFrom = cursor =>
      DefaultRankChannel(
        channelId = cursor.channel_id,
        subscriberAccountId = cursor.account_id
      )
  }
  override def extract[X: HasAccountId](db: SQLiteDatabase, id: X) = {
    val either = collector.extract(db, id)
    either.right map { _ map readable.convertFrom }
  }
}
