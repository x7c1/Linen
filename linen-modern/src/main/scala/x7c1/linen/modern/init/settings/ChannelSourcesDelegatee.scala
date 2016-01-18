package x7c1.linen.modern.init.settings

import android.app.Activity
import x7c1.linen.glue.activity.ActivityControl
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log

class ChannelSourcesDelegatee (
  activity: Activity with ActivityControl
){
  def setup(): Unit = {
    new ChannelSourcesMethods(activity).execute()
  }
  def close(): Unit = {
    Log info "[init]"
  }
}

class ChannelSourcesMethods(activity: Activity with ActivityControl){

  def execute() = IntentExpander executeBy activity

  def showSources(accountId: Long, channelId: Long): Unit = {
    Log info s"account:$accountId, channel:$channelId"
  }
}
