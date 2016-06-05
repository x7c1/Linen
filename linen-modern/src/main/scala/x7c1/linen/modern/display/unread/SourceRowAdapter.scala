package x7c1.linen.modern.display.unread

import x7c1.linen.glue.res.layout.{UnreadSourceRow, UnreadSourceRowFooter, UnreadSourceRowItem}
import x7c1.linen.repository.source.unread.{SourceRowContent, UnreadSource}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class SourceRowAdapter(
  delegatee: AdapterDelegatee[UnreadSourceRow, SourceRowContent],
  sourceSelectedListener: OnSourceSelectedListener,
  footerHeight: => Int ) extends BaseAdapter(delegatee){

  override def onBindViewHolder(row: UnreadSourceRow, position: Int) = {
    delegatee.bindViewHolder(row, position){
      case (holder: UnreadSourceRowItem, source: UnreadSource) =>
        holder.title.text = source.title
        holder.description.text = source.description
        holder.itemView onClick { view =>
          val event = SourceSelectedEvent(position, source)
          sourceSelectedListener onSourceSelected event
        }
      case (holder: UnreadSourceRowFooter, _) =>
        holder.itemView updateLayoutParams { _.height = footerHeight }
      case _ =>
        Log error s"unknown row: $row"
    }
  }
}

trait OnSourceSelectedListener {
  def onSourceSelected(event: SourceSelectedEvent): Unit
}

case class SourceSelectedEvent (position: Int, source: UnreadSource){
  def dump: String = s"sourceId:${source.id}, position:$position"
}
