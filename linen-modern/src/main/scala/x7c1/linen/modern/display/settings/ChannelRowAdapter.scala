package x7c1.linen.modern.display.settings

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{SettingMyChannelRow, SettingMyChannelRowFooter, SettingMyChannelRowItem}
import x7c1.linen.modern.accessor.AccountIdentifiable
import x7c1.linen.modern.accessor.setting.{MyChannel, MyChannelFooter, MyChannelRow}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.modern.decorator.Imports._

class ChannelRowAdapter(
  accountId: Long,
  delegatee: AdapterDelegatee[MyChannelRow, SettingMyChannelRow],
  onSourcesSelected: ChannelSourcesSelected => Unit,
  onSubscriptionChanged: MyChannelSubscriptionChanged => Unit
) extends Adapter[SettingMyChannelRow]{

  override def getItemCount: Int = delegatee.count

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    delegatee.createViewHolder(parent, viewType)
  }
  override def onBindViewHolder(holder: SettingMyChannelRow, position: Int): Unit = {
    delegatee.bindViewHolder(holder, position){
      case (holder: SettingMyChannelRowItem, channel: MyChannel) =>
        holder.name.text = channel.name
        holder.description.text = channel.description
        holder.sources onClick { _ =>
          onSourcesSelected apply ChannelSourcesSelected(
            accountId = accountId,
            channelId = channel.channelId,
            channelName = channel.name
          )
        }
        holder.switchSubscribe onChangedManually { e =>
          onSubscriptionChanged apply MyChannelSubscriptionChanged(
            accountId = accountId,
            channelId = channel.channelId,
            isSubscribed = e.isChecked
          )
        }
        holder.switchSubscribe setChecked channel.isSubscribed

      case (holder: SettingMyChannelRowFooter, footer: MyChannelFooter) =>
        // nop
    }
  }
  override def getItemViewType(position: Int) = delegatee.viewTypeAt(position)
}

case class ChannelSourcesSelected(
  accountId: Long,
  channelId: Long,
  channelName: String
)

case class MyChannelSubscriptionChanged(
  accountId: Long,
  channelId: Long,
  isSubscribed: Boolean
) extends AccountIdentifiable
