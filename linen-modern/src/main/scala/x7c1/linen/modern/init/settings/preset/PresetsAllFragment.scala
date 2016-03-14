package x7c1.linen.modern.init.settings.preset

import android.content.{Context, Intent}
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.view.{LayoutInflater, View, ViewGroup}
import x7c1.linen.glue.res.layout.{SettingPresetRow, SettingPresetTabAll}
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
  val rowFactory: ViewHolderProviderFactory[SettingPresetRow]
)
class PresetsAllFragment extends TypedFragment[ArgumentsForAll]{
  private lazy val args = getTypedArguments

  private lazy val helper = new LinenOpenHelper(getContext)

  override def onCreateView(
    inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {

    Log info s"[start]"

    val tab = args.tabFactory.create(inflater).inflateOn(container)
    PresetChannelsAccessor.create(args.accountId, helper) match {
      case Right(accessor) =>
        val manager = new LinearLayoutManager(getContext)
        tab.channelList setLayoutManager manager
        tab.channelList setAdapter new PresetsChannelsAdapter(
          listener =
            new SubscriptionChangedUpdater(args.accountId, helper) append
            new SubscriptionChangedNotifier(getContext, "hoge2"),
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

class SubscriptionChangedNotifier(
  context: Context,
  action: String ) extends ChannelSubscribedListener {

  override def onSubscribedChanged(event: SubscribeChangedEvent) = {
    val intent = new Intent(action)

    LocalBroadcastManager.getInstance(context) sendBroadcast intent

    LocalBroadcaster(event) dispatchFrom context
  }
}
class SubscriptionChangedUpdater(
  accountId0: Long, helper: LinenOpenHelper) extends ChannelSubscribedListener {

  override def onSubscribedChanged(event: SubscribeChangedEvent): Unit = {
    val account = new AccountIdentifiable {
      override def accountId: Long = accountId0
    }
    val subscriber = new ChannelSubscriber(account, helper)
    if (event.isChecked){
      subscriber subscribe event.channelId
    } else {
      subscriber unsubscribe event.channelId
    }
  }
}

trait ChannelSubscribedListener { self =>
  def onSubscribedChanged(event: SubscribeChangedEvent)
  def append(listener: ChannelSubscribedListener): ChannelSubscribedListener =
    new ChannelSubscribedListener {
      override def onSubscribedChanged(event: SubscribeChangedEvent): Unit = {
        self onSubscribedChanged event
        listener onSubscribedChanged event
      }
    }
}
