package x7c1.linen.modern.init.settings.preset

import x7c1.linen.database.struct.HasAccountId


case class PresetChannelSubscriptionChanged(
  channelId: Long,
  accountId: Long,
  isSubscribed: Boolean,
  from: PresetEventLocation
)

object PresetChannelSubscriptionChanged {
  implicit object account extends HasAccountId[PresetChannelSubscriptionChanged]{
    override def toId = _.accountId
  }
}
