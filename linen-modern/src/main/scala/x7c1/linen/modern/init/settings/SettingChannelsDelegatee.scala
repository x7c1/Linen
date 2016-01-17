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
    println(manager)

    val account = AccountAccessor.create(database) findAt 0 getOrElse {
      throw new IllegalStateException("account not found")
    }
    layout.channelList setLayoutManager manager
    layout.channelList setAdapter new ChannelRowAdapter(
      accessor = ChannelAccessor.create(database, account.accountId),
      viewHolderProvider = channelRowProvider
    )
  }
  def close(): Unit = {
    Log info "[done]"
  }
}
