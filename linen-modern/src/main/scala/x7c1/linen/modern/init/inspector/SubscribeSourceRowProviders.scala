package x7c1.linen.modern.init.inspector

import x7c1.linen.glue.res.layout.{SubscribeSourceRow, SubscribeSourceRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithSingleProvider
import x7c1.wheat.modern.resource.ViewHolderProviders

object SubscribeSourceRowProviders
  extends WithSingleProvider[SubscribeSourceRowProviders](_.forItem)

class SubscribeSourceRowProviders(
  val forItem: ViewHolderProvider[SubscribeSourceRowItem]
) extends ViewHolderProviders[SubscribeSourceRow]{

  override protected def all = Seq(
    forItem
  )
}
