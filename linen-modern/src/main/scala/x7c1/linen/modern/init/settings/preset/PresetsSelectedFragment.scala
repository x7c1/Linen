package x7c1.linen.modern.init.settings.preset

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener
import android.support.v7.widget.{LinearLayoutManager, PopupMenu}
import android.view.{LayoutInflater, Menu, MenuItem, View, ViewGroup}
import x7c1.linen.glue.res.layout.{SettingPresetChannelRow, SettingPresetTabSelected}
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.linen.modern.accessor.setting.SelectedChannelsAccessor
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.intent.LocalBroadcaster
import x7c1.wheat.macros.logger.Log


class ArgumentsForSelected(
  val accountId: Long,
  val tabFactory: ViewHolderProviderFactory[SettingPresetTabSelected],
  val rowFactory: ViewHolderProviderFactory[SettingPresetChannelRow]
)
class PresetsSelectedFragment extends TypedFragment[ArgumentsForSelected] with ReloadableFragment {
  private lazy val args = getTypedArguments

  private lazy val helper = new LinenOpenHelper(getContext)

  private lazy val presetsAccessor = SelectedChannelsAccessor.create(args.accountId, helper)

  private lazy val layout = args.tabFactory.createViewHolder(getView)

  override def reload(channelId: Long) = {
    Log info s"[start] $channelId"
    presetsAccessor.right.foreach(_.reload())
    layout.channelList.getAdapter.notifyDataSetChanged()
  }
  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    Log info s"[start]"

    val tab = args.tabFactory.create(inflater) inflateOn container
    presetsAccessor match {
      case Left(error) => Log error error.toString
      case Right(accessor) =>
        tab.channelList setLayoutManager new LinearLayoutManager(getContext)
        tab.channelList setAdapter new PresetsChannelsAdapter(
          listener = new SubscriptionChangedUpdater(args.accountId, getContext, helper),
          onSourceSelected = new OnSourcesSelected(activityControl).transitToSources,
          onMenuSelected = new OnMenuForSelected(getActivity, args.accountId).onSelected,
          accessor = accessor,
          provider = args.rowFactory create getContext,
          location = PresetTabSelected
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

class OnMenuForSelected(context: Context, accountId: Long) {
  def onSelected(e: MenuSelected) = {
    val menu = new PopupMenu(context, e.targetView)
    menu.getMenu.add(Menu.NONE, 123, 1, "Load all sources")
    menu.show()

    menu setOnMenuItemClickListener new OnMenuItemClickListener {
      override def onMenuItemClick(item: MenuItem): Boolean = {
        Log info s"[init] $item"

        if (item.getItemId == 123){
          val event = LoadSourcesEvent(e.channelId, accountId)
          LocalBroadcaster(event) dispatchFrom context
        }
        true
      }
    }
  }
}

case class LoadSourcesEvent(channelId: Long, accountId: Long)