package x7c1.linen.modern.init.settings

import android.app.Activity
import android.content.Intent
import x7c1.linen.glue.activity.ActivityControl
import x7c1.wheat.macros.intent.MethodCaller
import x7c1.wheat.macros.logger.Log

class ChannelSourcesDelegatee (
  activity: Activity with ActivityControl
){
  def setup(): Unit = {
    Option(activity.getIntent) match {
      case Some(intent) => Option(intent.getAction) match {
        case Some(_) =>
          new ChannelSourcesMethods(activity).executeBy(intent)
        case None =>
          Log error s"empty action"
      }
      case None =>
        Log error s"empty intent"
    }
  }
  def close(): Unit = {
    Log info "[init]"
  }
}

class ChannelSourcesMethods(activity: Activity with ActivityControl){

  def executeBy(intent: Intent): Unit = MethodCaller executeBy intent

  def showSources(accountId: Long, channelId: Long): Unit = {
    Log info s"account:$accountId, channel:$channelId"
  }
}
