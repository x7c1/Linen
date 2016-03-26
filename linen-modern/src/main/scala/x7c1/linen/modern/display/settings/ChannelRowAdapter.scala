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
  onSources: OnChannelSourcesListener,
  onSubscribeChanged: MyChannelSubscribeChanged => Unit ) extends Adapter[SettingMyChannelRow]{

  override def getItemCount: Int = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingMyChannelRow, position: Int): Unit = {
    accessor findAt position foreach { channel =>
      holder.name.text = channel.name
      holder.description.text = channel.description
      holder.sources onClick { _ =>
        onSources onSourcesSelected ChannelSourcesEvent(
          accountId = accessor.accountId,
          channelId = channel.channelId
        )
      }
      holder.switchSubscribe onChangedManually { e =>
        onSubscribeChanged apply MyChannelSubscribeChanged(
          channelId = channel.channelId,
          isSubscribed = e.isChecked
        )
      }
      holder.switchSubscribe setChecked channel.isSubscribed
    }
  }
}

trait OnChannelSourcesListener {
  def onSourcesSelected(event: ChannelSourcesEvent): Unit
}

case class ChannelSourcesEvent(
  accountId: Long,
  channelId: Long )

case class MyChannelSubscribeChanged(
  channelId: Long,
  isSubscribed: Boolean
)
