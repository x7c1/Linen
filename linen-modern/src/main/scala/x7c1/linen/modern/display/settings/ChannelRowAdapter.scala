package x7c1.linen.modern.display.settings

import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.res.layout.{SettingMyChannelRow, SettingMyChannelRowFooter, SettingMyChannelRowItem}
import x7c1.linen.repository.channel.my.{MyChannel, MyChannelFooter, MyChannelRow}
import x7c1.linen.scene.channel.menu.{MenuSelected, OnMenuSelectedListener}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.modern.decorator.Imports._

class ChannelRowAdapter[A: HasAccountId](
  account: A,
  delegatee: AdapterDelegatee[SettingMyChannelRow, MyChannelRow],
  onSourcesSelected: ChannelSourcesSelected => Unit,
  onMenuSelected: OnMenuSelectedListener[MyChannel],
  onSubscriptionChanged: MyChannelSubscriptionChanged => Unit
) extends BaseAdapter(delegatee){

  private val accountId = implicitly[HasAccountId[A]] toId account

  override def onBindViewHolder(holder: SettingMyChannelRow, position: Int): Unit = {
    delegatee.bindViewHolder(holder, position){
      case (holder: SettingMyChannelRowItem, channel: MyChannel) =>
        holder.name.text = channel.name
        holder.description toggleVisibility channel.description

        holder.menu onClick { view =>
          onMenuSelected onMenuSelected MenuSelected(view, channel)
        }
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
)
object MyChannelSubscriptionChanged {
  implicit object account extends HasAccountId[MyChannelSubscriptionChanged]{
    override def toId = _.accountId
  }
}
