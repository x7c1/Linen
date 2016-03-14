package x7c1.linen.modern.init.settings.preset

import android.content.{IntentFilter, Intent, Context, BroadcastReceiver}
import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager, FragmentPagerAdapter}
import android.support.v4.content.LocalBroadcastManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingPresetRow, SettingPresetChannelsLayout, SettingPresetTabAll, SettingPresetTabSelected}
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.FragmentFactory.create
import x7c1.wheat.macros.intent.{LocalBroadcastListener, IntentExpander}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class PresetChannelsDelegatee(
  activity: FragmentActivity with ActivityControl,
  layout: SettingPresetChannelsLayout,
  factories: ProviderFactories ){

  private lazy val receiver1 = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent): Unit = {
      Log info s"$intent"
    }
  }
  private lazy val receiver2 = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent): Unit = {
      Log info s"$intent"
    }
  }

  lazy val onSubscribe = LocalBroadcastListener[SubscribeChangedEvent]{
    event =>
      Log info s"${event.channelId}"
      println(event.channelId)
  }

  /*
  lazy val onSubscribe = LocalBroadcastListener[ChannelSubscribeEvent]{
    event => println(event.channelId)
  }
  =>
  lazy val onSubscribe = {
    new LocalBroadcastListener[ChannelSubscribeEvent](
      callback = {
        event => println(event.channelId)
      },
      receiver = new BroadcastReceiver {
        override def onReceive(context: Context, intent: Intent): Unit = {
          val event = ChannelSubscribeEvent(
            channelId = intent.getLongExtra("channelId", 0),
            isChecked = intent.getBooleanExtra("isChecked", false)
          )
          callback(event)
        }
      },
      intentFilter = {
        new IntentFilter("com.example.foo.bar.ChannelSubscribeEvent")
      }
    )
  }
  */

  def onCreate(): Unit = {
    Log info s"[start]"

    /*
    onSubscribe registerTo activity
    =>
    LocalBroadcastManager.getInstance(activity).registerReceiver(
      onSubscribe.receiver,
      onSubscribe.intentFilter
    )

    onSubscribe unregisterFrom activity
    =>
    LocalBroadcastManager.getInstance(activity).unregisterReceiver(
      onSubscribe.receiver
    )
    */
    LocalBroadcastManager.getInstance(activity).registerReceiver(
      receiver1, new IntentFilter("hoge1")
    )
    LocalBroadcastManager.getInstance(activity).registerReceiver(
      receiver2, new IntentFilter("hoge2")
    )
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    IntentExpander executeBy activity.getIntent
  }
  def showPresetChannels(accountId: Long) = {
    layout.pager setAdapter new PresetPagerAdapter(
      accountId = accountId,
      manager = activity.getSupportFragmentManager,
      factories = factories
    )
    layout.tabs.setupWithViewPager(layout.pager)
  }
  def onDestroy(): Unit = {
    Log info s"[start]"
    LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver1)
    LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver2)
  }
}

class ProviderFactories(
  val forSelected: ViewHolderProviderFactory[SettingPresetTabSelected],
  val forAll: ViewHolderProviderFactory[SettingPresetTabAll],
  val forRow: ViewHolderProviderFactory[SettingPresetRow]
)

class PresetPagerAdapter(
  accountId: Long,
  manager: FragmentManager,
  factories: ProviderFactories) extends FragmentPagerAdapter(manager) {

  lazy val fragments = Seq(
    "SELECTED" -> {
      create[PresetsSelectedFragment] by new ArgumentsForSelected(
        accountId,
        factories.forSelected,
        factories.forRow )
    },
    "ALL" -> {
      create[PresetsAllFragment] by new ArgumentsForAll(
        accountId,
        factories.forAll,
        factories.forRow )
    }
  )
  override def getItem(position: Int): Fragment = {
    fragments(position)._2
  }
  override def getPageTitle(position: Int): CharSequence = {
    fragments(position)._1
  }
  override def getCount: Int = fragments.length
}
