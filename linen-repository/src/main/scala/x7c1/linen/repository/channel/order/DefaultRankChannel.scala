package x7c1.linen.repository.channel.order

import x7c1.linen.database.struct.{ChannelStatusKey, HasChannelStatusKey}
import x7c1.wheat.modern.database.selector.SelectorProvidable

case class DefaultRankChannel(
  channelId: Long,
  subscriberAccountId: Long
)

object DefaultRankChannel {
  implicit object providable extends SelectorProvidable[
    DefaultRankChannel,
    DefaultRankChannelSelector
  ]
  implicit object collect extends CanCollectImpl

  implicit object key extends HasChannelStatusKey[DefaultRankChannel]{
    override def toId = channel =>
      ChannelStatusKey(
        channelId = channel.channelId,
        accountId = channel.subscriberAccountId
      )
  }
}
