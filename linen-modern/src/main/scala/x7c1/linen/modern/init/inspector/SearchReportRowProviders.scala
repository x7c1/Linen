package x7c1.linen.modern.init.inspector

import x7c1.linen.glue.res.layout.{SourceSearchRow, SourceSearchRowLoadingErrorItem, SourceSearchRowLoadingErrorLabel, SourceSearchRowSourceError, SourceSearchRowSourceItem, SourceSearchRowSourceLabel, SourceSearchRowSourceNotFound}
import x7c1.linen.repository.inspector.{DiscoveredLabelRow, DiscoveredSource, NoSourceFound, NoSourceFoundLabel, SourceLoadingError, SourceSearchReportRow, UrlLoadingError, UrlLoadingErrorLabel}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.ProviderSelectable
import x7c1.wheat.modern.resource.ViewHolderProviders
import x7c1.wheat.modern.sequence.Sequence

object SearchReportRowProviders {
  implicit def selectable[A <: Sequence[SourceSearchReportRow]]: ProviderSelectable[A, SearchReportRowProviders] =
    new ProviderSelectable[A, SearchReportRowProviders] {
      override def selectProvider(position: Int, sequence: A, providers: SearchReportRowProviders) = {
        sequence findAt position match {
          case Some(x: DiscoveredLabelRow) => providers.forSourceLabel
          case Some(x: DiscoveredSource) => providers.forSourceItem
          case Some(x: UrlLoadingErrorLabel) => providers.forErrorLabel
          case Some(x: UrlLoadingError) => providers.forErrorItem
          case Some(x: NoSourceFoundLabel) => providers.forErrorLabel
          case Some(x: NoSourceFound) => providers.forNoSource
          case Some(x: SourceLoadingError) => providers.forSourceError
          case _ => ???
        }
      }
    }
}

class SearchReportRowProviders (
  val forSourceLabel: ViewHolderProvider[SourceSearchRowSourceLabel],
  var forSourceItem: ViewHolderProvider[SourceSearchRowSourceItem],
  var forErrorLabel: ViewHolderProvider[SourceSearchRowLoadingErrorLabel],
  var forErrorItem: ViewHolderProvider[SourceSearchRowLoadingErrorItem],
  var forNoSource: ViewHolderProvider[SourceSearchRowSourceNotFound],
  var forSourceError: ViewHolderProvider[SourceSearchRowSourceError]
) extends ViewHolderProviders[SourceSearchRow]{

  override protected def all = Seq(
    forSourceLabel,
    forSourceItem,
    forErrorLabel,
    forErrorItem,
    forNoSource,
    forSourceError
  )
}
