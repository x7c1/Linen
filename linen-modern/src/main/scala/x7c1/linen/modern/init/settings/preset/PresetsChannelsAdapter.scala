package x7c1.linen.modern.init.settings.preset

import android.app.Activity
import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingPresetChannelRow
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.display.settings.ChannelSourcesSelected
import x7c1.linen.repository.channel.preset.{SettingPresetChannel, PresetChannelsAccessor}
import x7c1.linen.scene.channel.menu.{MenuSelected, OnChannelMenuSelected, OnMenuSelectedListener}
import x7c1.wheat.ancient.resource.{ViewHolderProvider, ViewHolderProviderFactory}
import x7c1.wheat.modern.decorator.Imports._

class PresetsChannelsAdapter(
  location: PresetEventLocation,
  listener: OnChannelSubscribedListener,
  onSourceSelected: ChannelSourcesSelected => Unit,
  onMenuSelected: OnMenuSelectedListener[SettingPresetChannel],
  accessor: PresetChannelsAccessor,
  provider: ViewHolderProvider[SettingPresetChannelRow]) extends Adapter[SettingPresetChannelRow] {

  override def getItemCount = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingPresetChannelRow, position: Int) = {
    accessor.findAt(position) foreach { channel =>
      holder.name.text = channel.name
      holder.description toggleVisibility channel.description

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
          channel.accountId,
          e.isChecked,
          location )
      }
      holder.switchSubscribe.checked = channel.isSubscribed
    }
  }
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
      onMenuSelected = OnChannelMenuSelected.forPresetChannel(activity, accountId),
      accessor = accessor,
      provider = factory create activity,
      location = location
    )
  }
}
