package x7c1.linen.modern.display.settings

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.SettingMyChannelRow
import x7c1.linen.modern.accessor.setting.MyChannelAccessor
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._

class ChannelRowAdapter(
  accessor: MyChannelAccessor,
  viewHolderProvider: ViewHolderProvider[SettingMyChannelRow],
  onSourcesSelected: ChannelSourcesSelected => Unit,
  onSubscriptionChanged: MyChannelSubscriptionChanged => Unit
) extends Adapter[SettingMyChannelRow]{

  override def getItemCount: Int = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingMyChannelRow, position: Int): Unit = {
    accessor findAt position foreach { channel =>
      holder.name.text = channel.name
      holder.description.text = channel.description
      holder.sources onClick { _ =>
        onSourcesSelected apply ChannelSourcesSelected(
          accountId = accessor.accountId,
          channelId = channel.channelId
        )
      }
      holder.switchSubscribe onChangedManually { e =>
        onSubscriptionChanged apply MyChannelSubscriptionChanged(
          channelId = channel.channelId,
          isSubscribed = e.isChecked
        )
      }
      holder.switchSubscribe setChecked channel.isSubscribed
    }
  }
}

case class ChannelSourcesSelected(
  accountId: Long,
  channelId: Long )

case class MyChannelSubscriptionChanged(
  channelId: Long,
  isSubscribed: Boolean
)
