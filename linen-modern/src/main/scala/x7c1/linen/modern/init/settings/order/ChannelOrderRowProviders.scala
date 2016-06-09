package x7c1.linen.modern.init.settings.order

import x7c1.linen.glue.res.layout.{SettingChannelOrderRow, SettingChannelOrderRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithSingleProvider
import x7c1.wheat.modern.resource.ViewHolderProviders

class ChannelOrderRowProviders(
  val forItem: ViewHolderProvider[SettingChannelOrderRowItem]
) extends ViewHolderProviders[SettingChannelOrderRow]{
  override protected def all = Seq(
    forItem
  )
}

object ChannelOrderRowProviders
  extends WithSingleProvider[ChannelOrderRowProviders](_.forItem)
