package x7c1.linen.modern.init.settings.preset

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.SettingPresetChannelRow
import x7c1.linen.modern.accessor.setting.PresetChannelsAccessor
import x7c1.linen.modern.display.settings.ChannelSourcesSelected
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._

class PresetsChannelsAdapter(
  location: PresetEventLocation,
  listener: OnChannelSubscribedListener,
  onSourceSelected: ChannelSourcesSelected => Unit,
  accessor: PresetChannelsAccessor,
  provider: ViewHolderProvider[SettingPresetChannelRow]) extends Adapter[SettingPresetChannelRow] {

  override def getItemCount = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingPresetChannelRow, position: Int) = {
    accessor.findAt(position) foreach { channel =>
      holder.name.text = channel.name
      holder.description.text = channel.description
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
