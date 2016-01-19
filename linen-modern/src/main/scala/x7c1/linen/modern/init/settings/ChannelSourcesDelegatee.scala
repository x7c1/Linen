package x7c1.linen.modern.init.settings

import android.app.Activity
import x7c1.linen.glue.activity.ActivityControl
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log

class ChannelSourcesDelegatee (
  activity: Activity with ActivityControl
){
  def setup(): Unit = {
    IntentExpander executeBy activity.getIntent
  }
  def close(): Unit = {
    Log info "[init]"
  }
  def showSources(accountId: Long, channelId: Long): Unit = {
    Log info s"account:$accountId, channel:$channelId"
  }
}
