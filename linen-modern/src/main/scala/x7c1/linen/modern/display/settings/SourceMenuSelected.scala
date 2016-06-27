package x7c1.linen.modern.display.settings

import android.view.View
import x7c1.linen.repository.source.setting.SettingSource

class SourceMenuSelected private (
  val targetView: View,
  val clientAccountId: Long,
  val channelId: Long,
  val selectedSourceId: Long
)

object SourceMenuSelected {
  def apply(
    targetView: View,
    clientAccountId: Long,
    channelId: Long,
    source: SettingSource): SourceMenuSelected = {

    new SourceMenuSelected(
      targetView,
      clientAccountId,
      channelId,
      source.sourceId)
  }
}
