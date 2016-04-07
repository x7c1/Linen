package x7c1.linen.modern.init.settings.my

import x7c1.linen.glue.res.layout.{SettingMyChannelRow, SettingMyChannelRowFooter, SettingMyChannelRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.{ProviderSelectable, FooterSelectable}
import x7c1.wheat.modern.resource.ViewHolderProviders
import x7c1.wheat.modern.sequence.Sequence

object MyChannelRowProviders {
  implicit def selectable[A <: Sequence[_]]: ProviderSelectable[A, MyChannelRowProviders] =
    new FooterSelectable(
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
