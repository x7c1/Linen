package x7c1.linen.modern.init.settings.preset

import android.app.Activity
import android.content.Intent
import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager, FragmentPagerAdapter}
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.SettingPresetChannelSources
import x7c1.linen.glue.res.layout.{SettingPresetChannelRow, SettingPresetChannelsLayout, SettingPresetTabAll, SettingPresetTabSelected}
import x7c1.linen.modern.display.settings.ChannelSourcesEvent
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.FragmentFactory.create
import x7c1.wheat.macros.intent.{IntentExpander, LocalBroadcastListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class PresetChannelsDelegatee(
  activity: FragmentActivity with ActivityControl,
  layout: SettingPresetChannelsLayout,
  factories: ProviderFactories ){

  lazy val onSubscribe = LocalBroadcastListener[SubscribeChangedEvent]{ event =>
    val reloadable: PartialFunction[Fragment, ReloadableFragment] = event.from match {
      case PresetTabSelected => { case f: PresetsAllFragment => f }
      case PresetTabAll => { case f: PresetsSelectedFragment => f }
    }
    allFragments collect reloadable foreach (_ reload event.channelId)
  }
  def allFragments = {
    (0 to layout.pager.getAdapter.getCount - 1).view map { n =>
      layout.pager.getAdapter.instantiateItem(layout.pager, n).asInstanceOf[Fragment]
    }
  }
  def onCreate(): Unit = {
    Log info s"[start]"
    onSubscribe registerTo activity

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
    onSubscribe unregisterFrom activity
  }
}

class ProviderFactories(
  val forSelected: ViewHolderProviderFactory[SettingPresetTabSelected],
  val forAll: ViewHolderProviderFactory[SettingPresetTabAll],
  val forRow: ViewHolderProviderFactory[SettingPresetChannelRow]
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

class OnSourcesSelected(activity: Activity with ActivityControl){
  def transitToSources(event: ChannelSourcesEvent): Unit = {
    Log info s"[init] $event"

    val intent = new Intent(activity, activity getClassOf SettingPresetChannelSources)
    activity startActivityBy intent
  }
}
