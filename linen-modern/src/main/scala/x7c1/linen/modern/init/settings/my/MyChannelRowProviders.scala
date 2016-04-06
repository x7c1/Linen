package x7c1.linen.modern.init.settings.my

import x7c1.linen.glue.res.layout.{SettingMyChannelRow, SettingMyChannelRowFooter, SettingMyChannelRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.FooterSelectable
import x7c1.wheat.modern.resource.ViewHolderProviders

object MyChannelRowProviders {
  implicit object selectable extends FooterSelectable[MyChannelRowProviders](
    footer = _.forFooter,
    other = _.forItem
  )
}

class MyChannelRowProviders(
  val forItem: ViewHolderProvider[SettingMyChannelRowItem],
  val forFooter: ViewHolderProvider[SettingMyChannelRowFooter]
) extends ViewHolderProviders[SettingMyChannelRow]{

  override protected def all = Seq(
    forItem,
    forFooter
  )
}
