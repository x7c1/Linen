package x7c1.linen.modern.init.inspector

import x7c1.linen.glue.res.layout.{SourceSearchRow, SourceSearchRowLoadingErrorItem, SourceSearchRowLoadingErrorLabel, SourceSearchRowSourceItem, SourceSearchRowSourceLabel}
import x7c1.linen.repository.inspector.{DiscoveredLabelRow, DiscoveredSource, SourceSearchReportRow, UrlLoadingError, UrlLoadingErrorLabel}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class SourceSearchRowAdapter(
  delegatee: AdapterDelegatee[SourceSearchRow, SourceSearchReportRow]
) extends BaseAdapter(delegatee){

  override def onBindViewHolder(holder: SourceSearchRow, position: Int): Unit = {
    delegatee.bindViewHolder(holder, position){
      case (row: SourceSearchRowSourceLabel, source: DiscoveredLabelRow) =>
        row.date.text = source.formattedDate
        row.message.text = source.reportMessage

      case (row: SourceSearchRowSourceItem, source: DiscoveredSource) =>
        row.title.text = source.sourceTitle
        row.url.text = source.sourceUrl

      case (row: SourceSearchRowLoadingErrorLabel, label: UrlLoadingErrorLabel) =>
        row.date.text = label.formattedDate
        row.message.text = label.reportMessage

      case (row: SourceSearchRowLoadingErrorItem, item: UrlLoadingError) =>
        row.message.text = item.errorText
        row.url.text = item.pageUrl

      case (row, item) =>
        Log info s"unknown row: $row, $item"
    }
  }
}
