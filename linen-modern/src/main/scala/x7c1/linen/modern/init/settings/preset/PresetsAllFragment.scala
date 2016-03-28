package x7c1.linen.modern.init.settings.preset

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.{LayoutInflater, View, ViewGroup}
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingPresetChannelRow, SettingPresetTabAll}
import x7c1.linen.glue.service.ServiceControl
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

  def activity: Activity with ActivityControl with ServiceControl =
    getActivity.asInstanceOf[Activity with ActivityControl with ServiceControl]

  protected def accountId: Long

  protected lazy val helper = new LinenOpenHelper(getContext)

  protected lazy val presetsAccessor = {
    PresetChannelsAccessor.create(accountId, helper) match {
      case Right(accessor) => Some(accessor)
      case Left(error) =>
        Log error error.toString
        None
    }
  }
}

class PresetsChannelsAdapterFactory(
  activity: Activity with ActivityControl with ServiceControl,
  factory: ViewHolderProviderFactory[SettingPresetChannelRow],
  location: PresetEventLocation,
  helper: LinenOpenHelper, accountId: Long){

  def createAdapter(accessor: PresetChannelsAccessor): PresetsChannelsAdapter = {
    new PresetsChannelsAdapter(
      listener = new SubscriptionChangedUpdater(accountId, activity, helper),
      onSourceSelected = new OnSourcesSelected(activity).transitToSources,
      onMenuSelected = new OnMenuForSelected(activity, accountId),
      accessor = accessor,
      provider = factory create activity,
      location = location
    )
  }
}

class PresetsAllFragment extends TypedFragment[ArgumentsForAll] with ReloadableFragment {
  private lazy val args = getTypedArguments

  private lazy val layout = args.tabFactory.createViewHolder(getView)

  private def toAdapter(accessor: PresetChannelsAccessor) = {
    val factory = new PresetsChannelsAdapterFactory(
      activity, args.rowFactory, PresetTabSelected, helper, args.accountId
    )
    factory.createAdapter(accessor)
  }
  override protected def accountId: Long = args.accountId

  override def reload(channelId: Long) = {
    Log info s"[start] $channelId"
    presetsAccessor foreach (_.reload())
    layout.channelList.getAdapter.notifyDataSetChanged()
  }
  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    Log info s"[start]"
    val tab = args.tabFactory.create(inflater).inflateOn(container)
    presetsAccessor map toAdapter foreach { adapter =>
      tab.channelList setLayoutManager new LinearLayoutManager(getContext)
      tab.channelList setAdapter adapter
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

class OnMenuForAll extends OnMenuSelectedListener {
  override def onMenuSelected(e: MenuSelected) = {

  }
}
