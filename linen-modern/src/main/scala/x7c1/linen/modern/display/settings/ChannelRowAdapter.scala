package x7c1.linen.modern.display.settings

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{SettingMyChannelRow, SettingMyChannelRowFooter, SettingMyChannelRowItem}
import x7c1.linen.modern.accessor.AccountIdentifiable
import x7c1.linen.modern.accessor.setting.{MyChannelAccessor, SettingMyChannel, SettingMyChannelFooter}
import x7c1.linen.modern.init.settings.my.MyChannelRowProviders
import x7c1.wheat.modern.decorator.Imports._

class ChannelRowAdapter(
  accessor: MyChannelAccessor,
  providers: MyChannelRowProviders,
  onSourcesSelected: ChannelSourcesSelected => Unit,
  onSubscriptionChanged: MyChannelSubscriptionChanged => Unit
) extends Adapter[SettingMyChannelRow]{

  override def getItemCount: Int = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    providers.createViewHolder(parent, viewType)
  }
  override def onBindViewHolder(holder: SettingMyChannelRow, position: Int): Unit = {
    accessor.bindViewHolder(holder, position){
      case (holder: SettingMyChannelRowItem, channel: SettingMyChannel) =>
        holder.name.text = channel.name
        holder.description.text = channel.description
        holder.sources onClick { _ =>
          onSourcesSelected apply ChannelSourcesSelected(
            accountId = accessor.accountId,
            channelId = channel.channelId,
            channelName = channel.name
          )
        }
        holder.switchSubscribe onChangedManually { e =>
          onSubscriptionChanged apply MyChannelSubscriptionChanged(
            accountId = accessor.accountId,
            channelId = channel.channelId,
            isSubscribed = e.isChecked
          )
        }
        holder.switchSubscribe setChecked channel.isSubscribed

      case (holder: SettingMyChannelRowFooter, footer: SettingMyChannelFooter) =>
        // nop
    }
  }
  private lazy val viewTypeAt = providers createViewTyper accessor

  override def getItemViewType(position: Int) = viewTypeAt(position)
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
