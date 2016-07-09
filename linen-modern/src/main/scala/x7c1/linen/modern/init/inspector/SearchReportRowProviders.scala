package x7c1.linen.modern.init.inspector

import x7c1.linen.glue.res.layout.{SourceSearchRow, SourceSearchRowSourceItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithSingleProvider
import x7c1.wheat.modern.resource.ViewHolderProviders

object SearchReportRowProviders
  extends WithSingleProvider[SearchReportRowProviders](_.forSourceItem)

class SearchReportRowProviders (
  var forSourceItem: ViewHolderProvider[SourceSearchRowSourceItem]
) extends ViewHolderProviders[SourceSearchRow]{

  override protected def all = Seq(forSourceItem)
}
