package x7c1.linen.scene.channel.menu

import android.view.View
import x7c1.linen.database.struct.ChannelIdentifiable
import x7c1.linen.repository.channel.my.MyChannel
import x7c1.linen.repository.channel.preset.SettingPresetChannel

class MenuSelected[A: ChannelIdentifiable] private (
  val targetView: View,
  val channel: A){

  def channelId: Long = implicitly[ChannelIdentifiable[A]] idOf channel
}

object MenuSelected {
  def apply(targetView: View, channel: SettingPresetChannel): MenuSelected[SettingPresetChannel] = {
    new MenuSelected(targetView, channel)
  }
  def apply(targetView: View, channel: MyChannel): MenuSelected[MyChannel] = {
    new MenuSelected(targetView, channel)
  }
}
