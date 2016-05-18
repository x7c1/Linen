package x7c1.linen.modern.init.settings.my

import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.SettingMyChannelSources
import x7c1.linen.glue.res.layout.{SettingMyChannelCreate, SettingMyChannelsLayout}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.display.settings.{ChannelRowAdapter, ChannelSourcesSelected, MyChannelSubscriptionChanged}
import x7c1.linen.repository.account.{AccountBase, ClientAccount}
import x7c1.linen.repository.channel.my.{MyChannelAccessorLoader, MyChannelRow}
import x7c1.linen.repository.channel.subscribe.ChannelSubscriber
import x7c1.linen.scene.channel.menu.OnChannelMenuSelected
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.fragment.FragmentFactory
import x7c1.wheat.macros.intent.{IntentExpander, IntentFactory, LocalBroadcastListener, LocalBroadcaster}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.sequence.Sequence

class MyChannelsDelegatee (
  activity: FragmentActivity with ActivityControl with ServiceControl,
  layout: SettingMyChannelsLayout,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  inputLayoutFactory: ViewHolderProviderFactory[SettingMyChannelCreate],
  channelRowProviders: MyChannelRowProviders ){

  private lazy val helper = new DatabaseHelper(activity)

  private lazy val database = helper.getReadableDatabase

  private lazy val loader = new MyChannelAccessorLoader(helper)

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
  private def reloadChannels(event: AccountBase): Unit ={
    val client = ClientAccount(event.accountId)
    (loader reload client){ _ =>
      layout.channelList.getAdapter.notifyDataSetChanged()
    }
  }
  private def setAdapter(account: ClientAccount)(accessor: Sequence[MyChannelRow]) = {
    Log info s"[init]"
    layout.channelList setAdapter new ChannelRowAdapter(
      accountId = account.accountId,
      delegatee = AdapterDelegatee.create(channelRowProviders, accessor),
      onSourcesSelected = new OnChannelSourcesSelected(activity).onSourcesSelected,
      onMenuSelected = OnChannelMenuSelected.forMyChannel(
        activity = activity,
        accountId = account.accountId,
        helper = helper,
        onDeleted = _ => (loader reload account){ _ =>
          layout.channelList.getAdapter.notifyDataSetChanged()
        }
      ),
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
        clientAccountId = accountId,
        dialogFactory = dialogFactory,
        inputLayoutFactory = inputLayoutFactory
      )

    fragment showIn activity
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
  helper: DatabaseHelper,
  account: AccountBase){

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
