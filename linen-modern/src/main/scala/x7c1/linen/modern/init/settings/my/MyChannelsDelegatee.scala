package x7c1.linen.modern.init.settings.my

import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.SettingMyChannelSources
import x7c1.linen.glue.res.layout.{SettingMyChannelRowItem, SettingMyChannelRowFooter, SettingMyChannelCreate, SettingMyChannelRow, SettingMyChannelsLayout}
import x7c1.linen.modern.accessor.database.ChannelSubscriber
import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.accessor.setting.{MyChannelAccessorLoader, MyChannelAccessor}
import x7c1.linen.modern.accessor.{AccountIdentifiable, LinenOpenHelper}
import x7c1.linen.modern.display.settings.{ChannelRowAdapter, ChannelSourcesSelected, MyChannelSubscriptionChanged}
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.{ViewHolderProvider, ViewHolderProviderFactory}
import x7c1.wheat.macros.fragment.FragmentFactory
import x7c1.wheat.macros.intent.{LocalBroadcastListener, IntentExpander, IntentFactory, LocalBroadcaster}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.resource.ViewHolderProviders

class MyChannelsDelegatee (
  activity: FragmentActivity with ActivityControl,
  layout: SettingMyChannelsLayout,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  inputLayoutFactory: ViewHolderProviderFactory[SettingMyChannelCreate],
  channelRowProviders: MyChannelRowProviders ){

  private lazy val helper = new LinenOpenHelper(activity)

  private lazy val database = helper.getReadableDatabase

  private lazy val loader = new MyChannelAccessorLoader(database)

  private lazy val onChannelCreated =
    LocalBroadcastListener[ChannelCreated]{ reloadChannels }

  private lazy val onSubscriptionChanged =
    LocalBroadcastListener[MyChannelSubscriptionChanged]{ reloadChannels }

  def setup(): Unit = {
    onChannelCreated registerTo activity
    onSubscriptionChanged registerTo activity

    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    val manager = new LinearLayoutManager(activity)
    layout.channelList setLayoutManager manager

    IntentExpander executeBy activity.getIntent
  }
  def showMyChannels(accountId: Long) = {
    val account = ClientAccount(accountId)
    loader.reload(account){ setAdapter(account) }
    layout.buttonToCreate onClick { _ => showInputDialog(accountId) }
  }
  def close(): Unit = {
    onChannelCreated unregisterFrom activity
    onSubscriptionChanged unregisterFrom activity
    database.close()
    helper.close()
    Log info "[done]"
  }
  private def reloadChannels(event: AccountIdentifiable): Unit ={
    val client = ClientAccount(event.accountId)
    (loader reload client){ _ =>
      layout.channelList.getAdapter.notifyDataSetChanged()
    }
  }
  private def setAdapter(account: ClientAccount)(accessor: MyChannelAccessor) = {
    layout.channelList setAdapter new ChannelRowAdapter(
      accessor = accessor,
      providers = channelRowProviders,
      onSourcesSelected = new OnChannelSourcesSelected(activity).onSourcesSelected,
      onSubscriptionChanged = {
        val listener = new OnMyChannelSubscriptionChanged(
          context = activity,
          helper = helper,
          account = account
        )
        listener.updateSubscription
      }
    )
  }
  private def showInputDialog(accountId: Long): Unit = {
    Log info s"[init] account:$accountId"

    val fragment = FragmentFactory.create[CreateChannelDialog] by
      new CreateChannelDialog.Arguments(
        accountId = accountId,
        dialogFactory = dialogFactory,
        inputLayoutFactory = inputLayoutFactory
      )

    fragment.show(activity.getSupportFragmentManager, "hoge")
  }
}

class OnChannelSourcesSelected(activity: Activity with ActivityControl){
  def onSourcesSelected(event: ChannelSourcesSelected): Unit = {
    Log info s"[init] $event"

    val intent = IntentFactory.using[MyChannelSourcesDelegatee].
      create(activity, activity getClassOf SettingMyChannelSources){
        _.showSources(event)
      }

    activity startActivity intent
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

class MyChannelRowProviders(
  val forItem: ViewHolderProvider[SettingMyChannelRowItem],
  val forFooter: ViewHolderProvider[SettingMyChannelRowFooter]
) extends ViewHolderProviders[SettingMyChannelRow]{

  override protected def all = Seq(
    forItem,
    forFooter
  )
  def createViewTyper(accessor: MyChannelAccessor): Int => Int = {
    position =>
      val provider = if (position == accessor.length - 1){
        forFooter
      } else {
        forItem
      }
      provider.layoutId()
  }
}
