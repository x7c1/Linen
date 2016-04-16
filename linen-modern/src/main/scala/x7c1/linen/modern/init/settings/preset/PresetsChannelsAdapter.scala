package x7c1.linen.modern.init.settings.preset

import android.app.Activity
import android.support.v7.widget.RecyclerView.Adapter
import android.view.{View, ViewGroup}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingPresetChannelRow
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.display.settings.ChannelSourcesSelected
import x7c1.linen.repository.channel.my.MyChannel
import x7c1.linen.repository.channel.preset.{PresetChannelsAccessor, SettingPresetChannel}
import x7c1.wheat.ancient.resource.{ViewHolderProvider, ViewHolderProviderFactory}
import x7c1.wheat.modern.decorator.Imports._

class PresetsChannelsAdapter(
  location: PresetEventLocation,
  listener: OnChannelSubscribedListener,
  onSourceSelected: ChannelSourcesSelected => Unit,
  onMenuSelected: OnMenuSelectedListener,
  accessor: PresetChannelsAccessor,
  provider: ViewHolderProvider[SettingPresetChannelRow]) extends Adapter[SettingPresetChannelRow] {

  override def getItemCount = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingPresetChannelRow, position: Int) = {
    accessor.findAt(position) foreach { channel =>
      holder.name.text = channel.name

      if (channel.description.isEmpty){
        holder.description setVisibility View.GONE
      } else {
        holder.description setVisibility View.VISIBLE
      }
      holder.description.text = channel.description

      holder.menu onClick { view =>
        onMenuSelected onMenuSelected MenuSelected(view, channel)
      }
      holder.sources onClick { _ =>
        onSourceSelected apply ChannelSourcesSelected(
          accountId = accessor.clientAccountId,
          channelId = channel.channelId,
          channelName = channel.name
        )
      }
      holder.switchSubscribe onChangedManually { e =>
        listener onSubscribedChanged PresetChannelSubscriptionChanged(
          channel.channelId,
          e.isChecked,
          location )
      }
      holder.switchSubscribe.checked = channel.isSubscribed
    }
  }
}

class MenuSelected private (
  val targetView: View, val channelId: Long)

object MenuSelected {
  def apply(targetView: View, channel: SettingPresetChannel): MenuSelected = {
    new MenuSelected(targetView, channel.channelId)
  }
  def apply(targetView: View, channel: MyChannel): MenuSelected = {
    new MenuSelected(targetView, channel.channelId)
  }
}

trait OnMenuSelectedListener {
  def onMenuSelected(e: MenuSelected): Unit
}

class PresetsChannelsAdapterFactory(
  activity: Activity with ActivityControl with ServiceControl,
  factory: ViewHolderProviderFactory[SettingPresetChannelRow],
  location: PresetEventLocation,
  helper: DatabaseHelper, accountId: Long){

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
