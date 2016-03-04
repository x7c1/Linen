package x7c1.linen.modern.init.settings.preset

import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager, FragmentPagerAdapter}
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingPresetRow, SettingPresetChannelsLayout, SettingPresetTabAll, SettingPresetTabSelected}
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.FragmentFactory.create
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class PresetChannelsDelegatee(
  activity: FragmentActivity with ActivityControl,
  layout: SettingPresetChannelsLayout,
  factories: ProviderFactories ){

  def onCreate(): Unit = {
    Log info s"[start]"

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
      create[SelectedChannelsFragment] by
        new ArgumentsForSelected(factories.forSelected)
    },
    "ALL" -> {
      create[PresetsAllFragment] by new ArgumentsForAll(
        accountId,
        factories.forAll,
        factories.forRow
      )
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
