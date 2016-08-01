package x7c1.linen.modern.init.inspector

import x7c1.linen.glue.res.layout.{SourceSearchRow, SourceSearchRowClientError, SourceSearchRowFooter, SourceSearchRowLabel, SourceSearchRowOriginError, SourceSearchRowSourceItem, SourceSearchRowSourceNotFound}
import x7c1.linen.repository.inspector.{DiscoveredLabelRow, DiscoveredSource, Footer, NoSourceFound, NoSourceFoundLabel, SourceLoadingError, SourceSearchReportRow, UrlLoadingError, UrlLoadingErrorLabel}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.ProviderSelectable
import x7c1.wheat.modern.resource.ViewHolderProviders
import x7c1.wheat.modern.sequence.Sequence

object SearchReportRowProviders {
  implicit def selectable[A <: Sequence[SourceSearchReportRow]]: ProviderSelectable[A, SearchReportRowProviders] =
    new ProviderSelectable[A, SearchReportRowProviders] {
      override def selectProvider(position: Int, sequence: A, providers: SearchReportRowProviders) = {
        sequence findAt position match {
          case Some(x: DiscoveredLabelRow) => providers.forLabel
          case Some(x: DiscoveredSource) => providers.forSourceItem
          case Some(x: UrlLoadingErrorLabel) => providers.forLabel
          case Some(x: UrlLoadingError) => providers.forErrorItem
          case Some(x: NoSourceFoundLabel) => providers.forLabel
          case Some(x: NoSourceFound) => providers.forNoSource
          case Some(x: SourceLoadingError) => providers.forClientError
          case Some(x: Footer) => providers.forFooter
          case _ => ???
        }
      }
    }
}

class SearchReportRowProviders (
  val forLabel: ViewHolderProvider[SourceSearchRowLabel],
  val forSourceItem: ViewHolderProvider[SourceSearchRowSourceItem],
  val forErrorItem: ViewHolderProvider[SourceSearchRowOriginError],
  val forNoSource: ViewHolderProvider[SourceSearchRowSourceNotFound],
  val forClientError: ViewHolderProvider[SourceSearchRowClientError],
  val forFooter: ViewHolderProvider[SourceSearchRowFooter]
) extends ViewHolderProviders[SourceSearchRow]{

  override protected def all = Seq(
    forLabel,
    forSourceItem,
    forErrorItem,
    forNoSource,
    forClientError,
    forFooter
  )
}
