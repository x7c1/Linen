package x7c1.linen.modern.display.settings

import android.view.View
import x7c1.linen.domain.account.ClientAccount
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
    clientAccount: ClientAccount,
    channelId: Long,
    source: SettingSource): SourceMenuSelected = {

    new SourceMenuSelected(
      targetView,
      clientAccount.accountId,
      channelId,
      source.sourceId)
  }
}
