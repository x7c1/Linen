package x7c1.linen.modern.init.settings.preset

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import x7c1.linen.glue.res.layout.SettingPresetTabSelected

import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment


class ArgumentsForSelected(
  val providerFactory: ViewHolderProviderFactory[SettingPresetTabSelected]
)
class SampleInvalidArguments(
  val providerFactory: ViewHolderProviderFactory[SettingPresetTabSelected]
)
class SelectedChannelsFragment extends TypedFragment[ArgumentsForSelected] {
  private lazy val args = getTypedArguments

  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    val tab = args.providerFactory.create(inflater).inflateOn(container)
    tab.itemView
  }
}

