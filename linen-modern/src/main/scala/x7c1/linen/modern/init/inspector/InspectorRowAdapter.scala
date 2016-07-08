package x7c1.linen.modern.init.inspector

import x7c1.linen.glue.res.layout.{InspectorRow, InspectorRowSourceItem}
import x7c1.linen.repository.inspector.{DiscoveredSource, InspectorReportRow}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class InspectorRowAdapter(
  delegatee: AdapterDelegatee[InspectorRow, InspectorReportRow]
) extends BaseAdapter(delegatee){

  override def onBindViewHolder(holder: InspectorRow, position: Int): Unit = {
    delegatee.bindViewHolder(holder, position){
      case (row: InspectorRowSourceItem, source: DiscoveredSource) =>
        row.body.text = source.label

      case (row, item) =>
        Log info s"unknown row: $row, $item"
    }
  }
}
