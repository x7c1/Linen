package x7c1.linen.repository.channel.subscribe

import x7c1.linen.database.mixin.SubscribedChannelRecord
import x7c1.linen.database.struct.{ChannelStatusKey, HasAccountId, HasChannelId, HasChannelStatusKey}
import x7c1.wheat.modern.database.selector.CursorConvertible
import x7c1.wheat.modern.database.selector.presets.{CanTraverseEntity, DefaultProvidable}
import x7c1.wheat.modern.features.HasShortLength

case class SubscribedChannel (
  channelId: Long,
  channelRank: Double,
  subscriberAccountId: Long,
  name: String
)

object SubscribedChannel {
  implicit object id extends HasChannelId[SubscribedChannel]{
    override def toId = _.channelId
  }
  implicit object account extends HasAccountId[SubscribedChannel]{
    override def toId = _.subscriberAccountId
  }
  implicit object key extends HasChannelStatusKey[SubscribedChannel]{
    override def toId = cursor =>
      ChannelStatusKey(
        channelId = cursor.channelId,
        accountId = cursor.subscriberAccountId
      )
  }
  implicit object convertible extends CursorConvertible[SubscribedChannelRecord, SubscribedChannel]{
    override def fromCursor = cursor =>
      SubscribedChannel(
        channelId = cursor.channel_id,
        channelRank = cursor.channel_rank,
        subscriberAccountId = cursor.account_id,
        name = cursor.name
      )
  }
  implicit object traversable extends CanTraverseEntity[
    HasAccountId,
    SubscribedChannelRecord,
    SubscribedChannel ]

  implicit object providable extends DefaultProvidable[
    HasAccountId,
    SubscribedChannel ]

  implicit object short extends HasShortLength[SubscribedChannel]
}
