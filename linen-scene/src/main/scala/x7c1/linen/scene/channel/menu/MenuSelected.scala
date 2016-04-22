package x7c1.linen.scene.channel.menu

import android.view.View
import x7c1.linen.repository.channel.my.MyChannel
import x7c1.linen.repository.channel.preset.SettingPresetChannel

class MenuSelected private (
  val targetView: View, val channelId: Long)

object MenuSelected {
  def apply(targetView: View, channel: SettingPresetChannel): MenuSelected = {
    new MenuSelected(targetView, channel.channelId)
  }
  def apply(targetView: View, channel: MyChannel): MenuSelected = {
    new MenuSelected(targetView, channel.channelId)
  }
}
