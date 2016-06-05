package x7c1.linen.repository.channel.preset

import x7c1.linen.database.struct.HasChannelId

case class SettingPresetChannel(
  channelId: Long,
  accountId: Long,
  name: String,
  description: String,
  isSubscribed: Boolean
)

object SettingPresetChannel {
  implicit object id extends HasChannelId[SettingPresetChannel] {
    override def toId = _.channelId
  }
}
