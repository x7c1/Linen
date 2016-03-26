package x7c1.linen.modern.init.settings

import android.app.Activity
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.SettingChannelSources
import x7c1.linen.glue.res.layout.{SettingChannelsLayout, SettingMyChannelRow}
import x7c1.linen.modern.accessor.database.ChannelSubscriber
import x7c1.linen.modern.accessor.setting.MyChannelAccessor
import x7c1.linen.modern.accessor.{AccountIdentifiable, LinenOpenHelper}
import x7c1.linen.modern.display.settings.{ChannelRowAdapter, ChannelSourcesEvent, MyChannelSubscribeChanged, OnChannelSourcesListener}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.{IntentExpander, IntentFactory, LocalBroadcaster}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class SettingChannelsDelegatee (
  activity: Activity with ActivityControl,
  layout: SettingChannelsLayout,
  channelRowProvider: ViewHolderProvider[SettingMyChannelRow] ){

  private lazy val helper = new LinenOpenHelper(activity)

  private lazy val database = helper.getReadableDatabase

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
      accessor = MyChannelAccessor.create(database, accountId),
      viewHolderProvider = channelRowProvider,
      onSources = new OnChannelSources(activity),
      onSubscribeChanged = {
        val listener = new MyChannelSubscribeChangedListener(
          context = activity,
          helper = helper,
          account = AccountIdentifiable(accountId)
        )
        listener.updateSubscription
      }
    )
  }
  def close(): Unit = {
    database.close()
    helper.close()
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

class MyChannelSubscribeChangedListener(
  context: Context,
  helper: LinenOpenHelper,
  account: AccountIdentifiable){

  def updateSubscription(event: MyChannelSubscribeChanged): Unit = {
    val subscriber = new ChannelSubscriber(account, helper)
    if (event.isSubscribed){
      subscriber subscribe event.channelId
    } else {
      subscriber unsubscribe event.channelId
    }
    LocalBroadcaster(event) dispatchFrom context
  }
}
