package x7c1.linen.modern.init.settings.preset

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.SettingPresetRow
import x7c1.linen.modern.accessor.setting.PresetChannelsAccessor
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._

class PresetsChannelsAdapter(
  location: PresetEventLocation,
  listener: OnChannelSubscribedListener,
  accessor: PresetChannelsAccessor,
  provider: ViewHolderProvider[SettingPresetRow]) extends Adapter[SettingPresetRow] {

  override def getItemCount = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingPresetRow, position: Int) = {
    accessor.findAt(position) foreach { channel =>
      holder.name.text = channel.name
      holder.description.text = channel.description
      holder.switchSubscribe.checked = channel.isSubscribed
      holder.switchSubscribe onChangedManually { e =>
        listener onSubscribedChanged SubscribeChangedEvent(
          channel.channelId,
          e.isChecked,
          location )
      }
    }
  }
}
