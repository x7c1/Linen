package x7c1.linen.modern.init.unread.source

import x7c1.linen.glue.res.layout.{UnreadSourceRow, UnreadSourceRowFooter, UnreadSourceRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.WithFooter
import x7c1.wheat.modern.resource.ViewHolderProviders

class SourceListProviders(
  val forItem: ViewHolderProvider[UnreadSourceRowItem],
  val forFooter: ViewHolderProvider[UnreadSourceRowFooter]
) extends ViewHolderProviders[UnreadSourceRow] {

  override protected val all = Seq(
    forItem,
    forFooter
  )
}

object SourceListProviders extends WithFooter[SourceListProviders](
  footer = _.forFooter,
  other = _.forItem
)
