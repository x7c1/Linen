package x7c1.linen.modern.init.inspector

import x7c1.linen.glue.res.layout.{SourceSearchRow, SourceSearchRowClientError, SourceSearchRowFooter, SourceSearchRowLabel, SourceSearchRowOriginError, SourceSearchRowSourceItem, SourceSearchRowSourceNotFound}
import x7c1.linen.repository.inspector.{ClientLoadingError, DiscoveredSource, DiscoveredSourceLabel, Footer, NoSourceFound, OriginLoadingError, SourceSearchReportRow}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class SourceSearchRowAdapter(
  delegatee: AdapterDelegatee[SourceSearchRow, SourceSearchReportRow]
) extends BaseAdapter(delegatee){

  override def onBindViewHolder(holder: SourceSearchRow, position: Int): Unit = {
    delegatee.bindViewHolder(holder, position){
      case (row: SourceSearchRowLabel, source: DiscoveredSourceLabel) =>
        row.date.text = source.formattedDate

      case (row: SourceSearchRowSourceItem, source: DiscoveredSource) =>
        row.title.text = source.sourceTitle
        row.url.text = source.sourceUrl

      case (row: SourceSearchRowOriginError, item: OriginLoadingError) =>
        row.message.text = item.errorText
        row.url.text = item.pageUrl

      case (row: SourceSearchRowSourceNotFound, item: NoSourceFound) =>
        row.title.text = item.reportMessage
        row.url.text = item.pageUrl

      case (row: SourceSearchRowClientError, item: ClientLoadingError) =>
        row.message.text = item.errorText
        row.url.text = item.pageUrl

      case (row: SourceSearchRowFooter, item: Footer) =>
        // nop

      case (row, item) =>
        Log info s"unknown row: $row, $item"
    }
  }
}
