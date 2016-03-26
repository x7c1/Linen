package x7c1.linen.modern.init.settings.preset

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.{LayoutInflater, View, ViewGroup}
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingPresetChannelRow, SettingPresetTabAll}
import x7c1.linen.modern.accessor.database.ChannelSubscriber
import x7c1.linen.modern.accessor.setting.PresetChannelsAccessor
import x7c1.linen.modern.accessor.{AccountIdentifiable, LinenOpenHelper}
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.intent.LocalBroadcaster
import x7c1.wheat.macros.logger.Log


class ArgumentsForAll(
  val accountId: Long,
  val tabFactory: ViewHolderProviderFactory[SettingPresetTabAll],
  val rowFactory: ViewHolderProviderFactory[SettingPresetChannelRow]
)

trait ReloadableFragment { self: Fragment =>
  def reload(channelId: Long): Unit
  def activityControl: Activity with ActivityControl =
    getActivity.asInstanceOf[Activity with ActivityControl]
}

class PresetsAllFragment extends TypedFragment[ArgumentsForAll] with ReloadableFragment {
  private lazy val args = getTypedArguments

  private lazy val helper = new LinenOpenHelper(getContext)

  private lazy val presetsAccessor = PresetChannelsAccessor.create(args.accountId, helper)

  private lazy val layout = args.tabFactory.createViewHolder(getView)

  override def reload(channelId: Long) = {
    Log info s"[start] $channelId"
    presetsAccessor.right.foreach(_.reload())
    layout.channelList.getAdapter.notifyDataSetChanged()
  }
  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    Log info s"[start]"

    val tab = args.tabFactory.create(inflater).inflateOn(container)
    presetsAccessor match {
      case Right(accessor) =>
        tab.channelList setLayoutManager new LinearLayoutManager(getContext)
        tab.channelList setAdapter new PresetsChannelsAdapter(
          listener = new SubscriptionChangedUpdater(args.accountId, getContext, helper),
          onSourceSelected = new OnSourcesSelected(activityControl).transitToSources,
          accessor = accessor,
          provider = args.rowFactory create inflater,
          location = PresetTabAll
        )
      case Left(error) => Log error error.toString
    }
    tab.itemView
  }
  override def onDestroy(): Unit = {
    Log info s"[start]"
    super.onDestroy()
    helper.close()
  }
}

class SubscriptionChangedUpdater(
  accountId0: Long, context: Context, helper: LinenOpenHelper) extends OnChannelSubscribedListener {

  override def onSubscribedChanged(event: PresetChannelSubscriptionChanged): Unit = {
    val account = new AccountIdentifiable {
      override def accountId: Long = accountId0
    }
    val subscriber = new ChannelSubscriber(account, helper)
    if (event.isSubscribed){
      subscriber subscribe event.channelId
    } else {
      subscriber unsubscribe event.channelId
    }
    LocalBroadcaster(event) dispatchFrom context
  }
}

trait OnChannelSubscribedListener { self =>
  def onSubscribedChanged(event: PresetChannelSubscriptionChanged): Unit
}
