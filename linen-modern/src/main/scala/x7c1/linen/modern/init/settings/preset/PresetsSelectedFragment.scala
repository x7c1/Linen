package x7c1.linen.modern.init.settings.preset

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener
import android.view.{LayoutInflater, Menu, MenuItem, View, ViewGroup}
import x7c1.linen.glue.res.layout.{SettingPresetChannelRow, SettingPresetTabSelected}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.init.updater.UpdaterMethods
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log

class ArgumentsForSelected(
  val accountId: Long,
  val tabFactory: ViewHolderProviderFactory[SettingPresetTabSelected],
  val rowFactory: ViewHolderProviderFactory[SettingPresetChannelRow]
) extends PresetFragmentArguments

class PresetsSelectedFragment extends TypedFragment[ArgumentsForSelected] with PresetFragment {
  protected lazy val args = getTypedArguments
  private lazy val layout = args.tabFactory.createViewHolder(getView)

  override def reload(channelId: Long) = {
    Log info s"[start] $channelId"
    presetsAccessor foreach (_.reload())
    layout.channelList.getAdapter.notifyDataSetChanged()
  }
  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    Log info s"[start]"
    val tab = args.tabFactory.create(inflater) inflateOn container
    applyAdapterTo(tab.channelList, from = PresetTabSelected)
    tab.itemView
  }
  override def onDestroy(): Unit = {
    Log info s"[start]"
    super.onDestroy()
    helper.close()
  }
}

class OnMenuForSelected(
  activity: Activity with ServiceControl,
  accountId: Long) extends OnMenuSelectedListener {

  override def onMenuSelected(e: MenuSelected) = {
    val menu = new PopupMenu(activity, e.targetView)
    menu.getMenu.add(Menu.NONE, 123, 1, "Load all sources")
    menu.show()

    menu setOnMenuItemClickListener new OnMenuItemClickListener {
      override def onMenuItemClick(item: MenuItem): Boolean = {
        Log info s"[init] $item"

        if (item.getItemId == 123){
          ServiceCaller.using[UpdaterMethods].
            startService(activity, activity getClassOf Updater){
              _ loadChannelSources (e.channelId, accountId)
            }
        }
        true
      }
    }
  }
}

case class LoadSourcesEvent(channelId: Long, accountId: Long)