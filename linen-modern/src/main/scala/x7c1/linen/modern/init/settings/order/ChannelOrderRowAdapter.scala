package x7c1.linen.modern.init.settings.order

import x7c1.linen.glue.res.layout.{SettingChannelOrderRow, SettingChannelOrderRowItem}
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class ChannelOrderRowAdapter(
  delegatee: AdapterDelegatee[SettingChannelOrderRow, SubscribedChannel]
) extends BaseAdapter(delegatee){
  override def onBindViewHolder(holder: SettingChannelOrderRow, i: Int): Unit = {
    delegatee.bindViewHolder(holder, i){
      case (row: SettingChannelOrderRowItem, channel) =>
        row.name.text = channel.name
      case (x, y) =>
        Log info s"unknown row: $x $y"
    }

  }
}
