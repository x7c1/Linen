package x7c1.linen.modern.init.settings.preset

import android.os.Bundle
import android.view.{View, ViewGroup, LayoutInflater}
import x7c1.linen.glue.res.layout.SettingPresetTabAll
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment


class ArgumentsForAll(
  val providerFactory: ViewHolderProviderFactory[SettingPresetTabAll]
)
class AllPresetsFragment extends TypedFragment[ArgumentsForAll]{
  private lazy val args = getTypedArguments

  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    val tab = args.providerFactory.create(inflater).inflateOn(container)
    tab.itemView
  }

}
