package x7c1.linen.modern.init.inspector

import x7c1.linen.glue.res.layout.{InspectorRow, InspectorRowSourceItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithSingleProvider
import x7c1.wheat.modern.resource.ViewHolderProviders

object InspectorRowProviders
  extends WithSingleProvider[InspectorRowProviders](_.forSourceItem)

class InspectorRowProviders (
  var forSourceItem: ViewHolderProvider[InspectorRowSourceItem]
) extends ViewHolderProviders[InspectorRow]{

  override protected def all = Seq(forSourceItem)
}
