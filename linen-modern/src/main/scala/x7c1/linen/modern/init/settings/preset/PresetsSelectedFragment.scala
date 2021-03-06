package x7c1.linen.modern.init.settings.preset

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import x7c1.linen.glue.res.layout.{SettingPresetChannelRow, SettingPresetTabSelected}
import x7c1.linen.repository.channel.preset.SelectedPresetChannelsAccessor
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log

class ArgumentsForSelected(
  val accountId: Long,
  val tabFactory: ViewHolderProviderFactory[SettingPresetTabSelected],
  val rowFactory: ViewHolderProviderFactory[SettingPresetChannelRow]
) extends PresetFragmentArguments

class PresetsSelectedFragment extends TypedFragment[ArgumentsForSelected] with PresetFragment {
  protected lazy val args = getTypedArguments

  private lazy val layout = args.tabFactory.createViewHolder(getView)

  override protected def accessorFactory = SelectedPresetChannelsAccessor

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


