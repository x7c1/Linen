package x7c1.linen.modern.display.settings

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.SettingChannelsRow
import x7c1.linen.modern.accessor.ChannelAccessor
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._

class ChannelRowAdapter(
  accessor: ChannelAccessor,
  viewHolderProvider: ViewHolderProvider[SettingChannelsRow]) extends Adapter[SettingChannelsRow]{

  override def getItemCount: Int = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingChannelsRow, position: Int): Unit = {
    accessor findAt position foreach { channel =>
      holder.name.text = channel.name
      holder.description.text = channel.description
    }
  }
}
