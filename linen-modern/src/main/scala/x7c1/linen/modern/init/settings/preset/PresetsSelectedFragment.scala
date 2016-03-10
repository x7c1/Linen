package x7c1.linen.modern.init.settings.preset

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.{LayoutInflater, View, ViewGroup}
import x7c1.linen.glue.res.layout.{SettingPresetRow, SettingPresetTabSelected}
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.linen.modern.accessor.setting.SelectedChannelsAccessor
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log


class ArgumentsForSelected(
  val accountId: Long,
  val tabFactory: ViewHolderProviderFactory[SettingPresetTabSelected],
  val rowFactory: ViewHolderProviderFactory[SettingPresetRow]
)
class PresetsSelectedFragment extends TypedFragment[ArgumentsForSelected] {
  private lazy val args = getTypedArguments

  private lazy val helper = new LinenOpenHelper(getContext)

  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    Log info s"[start]"

    val tab = args.tabFactory.create(inflater) inflateOn container
    SelectedChannelsAccessor.create(args.accountId, helper) match {
      case Left(error) => Log error error.toString
      case Right(accessor) =>
        tab.channelList setLayoutManager new LinearLayoutManager(getContext)
        tab.channelList setAdapter new PresetsAllAdapter(
          listener = new OnChannelSubscribed("hoge1", getContext, args.accountId, helper),
          accessor = accessor,
          provider = args.rowFactory create getContext
        )
    }
    tab.itemView
  }
  override def onDestroy(): Unit = {
    Log info s"[start]"
    super.onDestroy()
    helper.close()
  }
}
