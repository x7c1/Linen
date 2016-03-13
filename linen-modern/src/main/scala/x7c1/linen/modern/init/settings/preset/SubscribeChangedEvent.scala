package x7c1.linen.modern.init.settings.preset


case class SubscribeChangedEvent(
  channelId: Long,
  isChecked: Boolean,
  from: PresetEventLocation
)
