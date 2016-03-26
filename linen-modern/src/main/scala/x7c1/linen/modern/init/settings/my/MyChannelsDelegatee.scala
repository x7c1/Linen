package x7c1.linen.modern.init.settings.my

import android.app.Activity
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.SettingMyChannelSources
import x7c1.linen.glue.res.layout.{SettingMyChannelRow, SettingMyChannelsLayout}
import x7c1.linen.modern.accessor.database.ChannelSubscriber
import x7c1.linen.modern.accessor.setting.MyChannelAccessor
import x7c1.linen.modern.accessor.{AccountIdentifiable, LinenOpenHelper}
import x7c1.linen.modern.display.settings.{ChannelRowAdapter, ChannelSourcesSelected, MyChannelSubscriptionChanged}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.{IntentExpander, IntentFactory, LocalBroadcaster}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class MyChannelsDelegatee (
  activity: Activity with ActivityControl,
  layout: SettingMyChannelsLayout,
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
      onSourcesSelected = new OnChannelSourcesSelected(activity).onSourcesSelected,
      onSubscriptionChanged = {
        val listener = new OnMyChannelSubscriptionChanged(
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

class OnChannelSourcesSelected(activity: Activity with ActivityControl){
  def onSourcesSelected(event: ChannelSourcesSelected): Unit = {
    Log info s"[init] $event"

    val intent = IntentFactory.using[MyChannelSourcesDelegatee].
      create(activity, activity getClassOf SettingMyChannelSources){
        _.showSources(event.accountId, event.channelId)
      }

    activity startActivityBy intent
  }
}

class OnMyChannelSubscriptionChanged(
  context: Context,
  helper: LinenOpenHelper,
  account: AccountIdentifiable){

  def updateSubscription(event: MyChannelSubscriptionChanged): Unit = {
    val subscriber = new ChannelSubscriber(account, helper)
    if (event.isSubscribed){
      subscriber subscribe event.channelId
    } else {
      subscriber unsubscribe event.channelId
    }
    LocalBroadcaster(event) dispatchFrom context
  }
}
