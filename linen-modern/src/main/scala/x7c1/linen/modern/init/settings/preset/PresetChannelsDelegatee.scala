package x7c1.linen.modern.init.settings.preset

import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager, FragmentPagerAdapter}
import android.view.{View, ViewGroup, LayoutInflater}
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingPresetTab, SettingPresetTabSelected, SettingPresetTabAll, SettingPresetChannelsLayout}
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
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
    layout.pager setAdapter new PresetPagerAdapter(
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
  val forAll: ViewHolderProviderFactory[SettingPresetTabAll]
)

class PresetPagerAdapter(
  manager: FragmentManager,
  factories: ProviderFactories) extends FragmentPagerAdapter(manager) {

  override def getItem(position: Int): Fragment = {
    val factory = position match {
      case 0 => factories.forSelected
      case _ => factories.forAll
    }
    PresetFragment.newInstance(position + 1, factory)
  }
  override def getPageTitle(position: Int): CharSequence = {
    s"tab ${position + 1}"
  }
  override def getCount: Int = 2
}

class PresetFragment extends Fragment {

  override def onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedInstanceState: Bundle): View = {

    val page = getArguments.getInt("page")
    val factory = getArguments.
      getSerializable("factory").asInstanceOf[ViewHolderProviderFactory[SettingPresetTab]]

    val view = factory.create(inflater).inflateOn(container)
    view.itemView
  }
}

object PresetFragment {
  def newInstance[A](page: Int, factory: ViewHolderProviderFactory[A]): PresetFragment = {
    val fragment = new PresetFragment
    val bundle = new Bundle
    bundle.putInt("page", page)
    bundle.putSerializable("factory", factory)
    fragment.setArguments(bundle)
    fragment
  }
}

class PresetFragmentDelegatee()
