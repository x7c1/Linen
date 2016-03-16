package x7c1.linen.modern.init.settings

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.SettingChannelSources
import x7c1.linen.glue.res.layout.{SettingChannelsLayout, SettingChannelsRow}
import x7c1.linen.modern.accessor.{ChannelAccessor, LinenOpenHelper}
import x7c1.linen.modern.display.settings.{ChannelRowAdapter, ChannelSourcesEvent, OnChannelSourcesListener}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.{IntentExpander, IntentFactory}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class SettingChannelsDelegatee (
  activity: Activity with ActivityControl,
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

    IntentExpander executeBy activity.getIntent
  }
  def showMyChannels(accountId: Long) = {
    layout.channelList setAdapter new ChannelRowAdapter(
      accessor = ChannelAccessor.create(database, accountId),
      viewHolderProvider = channelRowProvider,
      onSources = new OnChannelSources(activity)
    )
  }
  def close(): Unit = {
    database.close()
    Log info "[done]"
  }
}

class OnChannelSources(activity: Activity with ActivityControl)
  extends OnChannelSourcesListener {

  override def onSourcesSelected(event: ChannelSourcesEvent): Unit = {
    Log info s"[init] $event"

    val intent = IntentFactory.using[ChannelSourcesDelegatee].
      create(activity, activity getClassOf SettingChannelSources){
        _.showSources(event.accountId, event.channelId)
      }

    activity startActivityBy intent
  }
}
