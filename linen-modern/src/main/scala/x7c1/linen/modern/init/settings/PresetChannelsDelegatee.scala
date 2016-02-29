package x7c1.linen.modern.init.settings

import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager, FragmentPagerAdapter}
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingPresetChannelsLayout
import x7c1.wheat.macros.logger.Log

class PresetChannelsDelegatee(
  activity: FragmentActivity with ActivityControl,
  layout: SettingPresetChannelsLayout ){

  def onCreate(): Unit = {
    Log info s"start"

    layout.pager.setAdapter(new PresetPagerAdapter(activity.getSupportFragmentManager))
    layout.tabs.setupWithViewPager(layout.pager)
  }

  def onDestroy(): Unit = {
    Log info s"start"
  }
}

class PresetPagerAdapter(
  manager: FragmentManager) extends FragmentPagerAdapter(manager) {

  override def getItem(position: Int): Fragment = {
    PresetFragment.newInstance(position + 1)
  }
  override def getPageTitle(position: Int): CharSequence = {
    s"tab ${position + 1}"
  }
  override def getCount: Int = 3
}

class PresetFragment extends Fragment {

}

object PresetFragment {
  def newInstance(page: Int): PresetFragment = {
    val fragment = new PresetFragment
    val bundle = new Bundle
    bundle.putInt("page", page)
    fragment.setArguments(bundle)
    fragment
  }
}