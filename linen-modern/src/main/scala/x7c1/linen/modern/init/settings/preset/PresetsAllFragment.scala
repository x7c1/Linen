package x7c1.linen.modern.init.settings.preset

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView.Adapter
import android.view.{LayoutInflater, View, ViewGroup}
import x7c1.linen.glue.res.layout.{SettingPresetRow, SettingPresetTabAll}
import x7c1.linen.modern.accessor.database.ChannelSubscriber
import x7c1.linen.modern.accessor.setting.PresetChannelsAccessor
import x7c1.linen.modern.accessor.{AccountIdentifiable, LinenOpenHelper}
import x7c1.wheat.ancient.resource.{ViewHolderProvider, ViewHolderProviderFactory}
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


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
        tab.channelList setAdapter new PresetsAllAdapter(
          listener = new OnChannelSubscribed(args.accountId, helper),
          accessor = accessor,
          provider = args.rowFactory create inflater
        )
      case Left(error) => Log error error.toString
    }
    tab.itemView
  }
  override def onDestroy(): Unit = {
    super.onDestroy()
    helper.close()
  }
}

class OnChannelSubscribed(accountId0: Long, helper: LinenOpenHelper) extends ChannelSubscribedListener {
  override def onSubscribedChanged(event: ChannelSubscribeEvent): Unit = {
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

class PresetsAllAdapter(
  listener: ChannelSubscribedListener,
  accessor: PresetChannelsAccessor,
  provider: ViewHolderProvider[SettingPresetRow]) extends Adapter[SettingPresetRow] {

  override def getItemCount = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingPresetRow, position: Int) = {
    Log info s"[start]"

    accessor.findAt(position) foreach { channel =>
      holder.name.text = channel.name
      holder.description.text = channel.description
      holder.switchSubscribe.checked = channel.isSubscribed
      holder.switchSubscribe onCheckedChanged { e =>
        listener onSubscribedChanged ChannelSubscribeEvent(channel.channelId, e.isChecked)
      }
      Log info s"${channel.name}"
    }
  }
}

trait ChannelSubscribedListener {
  def onSubscribedChanged(event: ChannelSubscribeEvent)
}

case class ChannelSubscribeEvent(
  channelId: Long,
  isChecked: Boolean
)
