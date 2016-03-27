package x7c1.linen.modern.init.settings.preset


case class PresetChannelSubscriptionChanged(
  channelId: Long,
  isSubscribed: Boolean,
  from: PresetEventLocation
)
