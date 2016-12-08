package x7c1.linen.repository.inspector

import x7c1.wheat.modern.database.selector.SelectorProvidable

sealed trait SourceSearchReportRow

object SourceSearchReportRow {

  implicit object traverse extends TraverseReport

  implicit object providable extends SelectorProvidable[SourceSearchReportRow, ReportSelector]

}

case class DiscoveredSourceLabel(
  formattedDate: String,
  reportMessage: String,
  pageTitle: String,
  pageUrl: String) extends SourceSearchReportRow

case class DiscoveredSource(
  sourceId: Long,
  sourceTitle: String,
  sourceDescription: String,
  sourceUrl: String) extends SourceSearchReportRow

case class OriginLoadingError(
  errorText: String,
  pageUrl: String) extends SourceSearchReportRow

case class ClientLoadingError(
  errorText: String,
  pageUrl: String) extends SourceSearchReportRow

case class NoSourceFound(
  pageTitle: String,
  pageUrl: String,
  reportMessage: String) extends SourceSearchReportRow

case class Footer() extends SourceSearchReportRow