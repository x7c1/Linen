package x7c1.linen.repository.channel.preset

import x7c1.linen.repository.channel.ChannelIdentifiable

case class SettingPresetChannel(
  channelId: Long,
  name: String,
  description: String,
  isSubscribed: Boolean
)

object SettingPresetChannel {
  implicit object id extends ChannelIdentifiable[SettingPresetChannel] {
    override def channelId(target: SettingPresetChannel): Long = target.channelId
  }
}
