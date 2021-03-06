package x7c1.linen.modern.init.settings.preset

import android.content.Context
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.res.layout.{SettingPresetChannelRow, SettingPresetTabAll}
import x7c1.linen.repository.account.AccountBase
import x7c1.linen.repository.channel.preset.AllPresetChannelsAccessor
import x7c1.linen.repository.channel.subscribe.ChannelSubscriber
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.intent.LocalBroadcaster
import x7c1.wheat.macros.logger.Log


class ArgumentsForAll(
  val accountId: Long,
  val tabFactory: ViewHolderProviderFactory[SettingPresetTabAll],
  val rowFactory: ViewHolderProviderFactory[SettingPresetChannelRow]
) extends PresetFragmentArguments

class PresetsAllFragment extends TypedFragment[ArgumentsForAll] with PresetFragment {
  protected lazy val args = getTypedArguments

  private lazy val layout = args.tabFactory.createViewHolder(getView)

  override protected def accessorFactory = AllPresetChannelsAccessor

  override def reload(channelId: Long) = {
    Log info s"[start] $channelId"
    presetsAccessor foreach (_.reload())
    layout.channelList.getAdapter.notifyDataSetChanged()
  }
  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    Log info s"[start]"
    val tab = args.tabFactory.create(inflater).inflateOn(container)
    applyAdapterTo(tab.channelList, from = PresetTabAll)
    tab.itemView
  }
  override def onDestroy(): Unit = {
    Log info s"[start]"
    super.onDestroy()
    helper.close()
  }

}

class SubscriptionChangedUpdater(
  accountId0: Long, context: Context, helper: DatabaseHelper) extends OnChannelSubscribedListener {

  override def onSubscribedChanged(event: PresetChannelSubscriptionChanged): Unit = {
    val account = new AccountBase {
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
