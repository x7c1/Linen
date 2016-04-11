package x7c1.linen.modern.init.settings.preset

import x7c1.linen.glue.res.layout.{SettingSourceAttachRow, SettingSourceAttachRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithSingleProvider
import x7c1.wheat.modern.resource.ViewHolderProviders

object SettingSourceAttachRowProviders
  extends WithSingleProvider[SettingSourceAttachRowProviders](_.forItem)

class SettingSourceAttachRowProviders(
  val forItem: ViewHolderProvider[SettingSourceAttachRowItem]
) extends ViewHolderProviders[SettingSourceAttachRow]{
  override protected def all = Seq(
    forItem
  )
}
