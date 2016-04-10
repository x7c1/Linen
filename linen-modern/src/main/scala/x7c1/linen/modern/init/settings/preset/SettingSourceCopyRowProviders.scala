package x7c1.linen.modern.init.settings.preset

import x7c1.linen.glue.res.layout.{SettingSourceCopyRow, SettingSourceCopyRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithSingleProvider
import x7c1.wheat.modern.resource.ViewHolderProviders

object SettingSourceCopyRowProviders
  extends WithSingleProvider[SettingSourceCopyRowProviders](_.forItem)

class SettingSourceCopyRowProviders(
  val forItem: ViewHolderProvider[SettingSourceCopyRowItem]
) extends ViewHolderProviders[SettingSourceCopyRow]{
  override protected def all = Seq(
    forItem
  )
}
