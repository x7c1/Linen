package x7c1.linen.modern.init.settings

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.res.layout.{SettingChannelsRow, SettingChannelsLayout}
import x7c1.linen.modern.accessor.{AccountAccessor, LinenOpenHelper, ChannelAccessor}
import x7c1.linen.modern.display.settings.ChannelRowAdapter
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class SettingChannelsDelegatee (
  activity: Activity,
  layout: SettingChannelsLayout,
  channelRowProvider: ViewHolderProvider[SettingChannelsRow] ){

  private lazy val database =
    new LinenOpenHelper(activity).getReadableDatabase

  def setup(): Unit = {
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    val manager = new LinearLayoutManager(activity)
    layout.channelList setLayoutManager manager

    AccountAccessor findCurrentAccountId database match {
      case Some(accountId) =>
        layout.channelList setAdapter new ChannelRowAdapter(
          accessor = ChannelAccessor.create(database, accountId),
          viewHolderProvider = channelRowProvider
        )
      case None =>
        Log warn "account not found"
    }
  }
  def close(): Unit = {
    Log info "[done]"
  }
}
