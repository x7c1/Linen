package x7c1.linen.modern.init.settings

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingChannelSourcesLayout
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class ChannelSourcesDelegatee (
  activity: Activity with ActivityControl,
  layout: SettingChannelSourcesLayout
){
  private lazy val database =
    new LinenOpenHelper(activity).getReadableDatabase

  def setup(): Unit = {
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    val manager = new LinearLayoutManager(activity)
    layout.sourceList setLayoutManager manager

    IntentExpander executeBy activity.getIntent
  }
  def close(): Unit = {
    database.close()
    Log info "[init]"
  }
  def showSources(accountId: Long, channelId: Long): Unit = {
    Log info s"account:$accountId, channel:$channelId"
  }
}
