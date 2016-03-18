package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{UnreadSourceRowItem, UnreadSourceRow}
import x7c1.linen.modern.accessor.UnreadSourceAccessor
import x7c1.linen.modern.struct.UnreadSource
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class SourceRowAdapter(
  sourceAccessor: UnreadSourceAccessor,
  sourceSelectedListener: OnSourceSelectedListener,
  viewHolderProvider: ViewHolderProvider[UnreadSourceRowItem]) extends Adapter[UnreadSourceRow]{

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }

  override def onBindViewHolder(row: UnreadSourceRow, position: Int) = {
    sourceAccessor.findAt(position) -> row match {
      case (Some(source), holder: UnreadSourceRowItem) =>
        holder.title.text = source.title
        holder.description.text = source.description
        holder.itemView onClick { view =>
          val event = SourceSelectedEvent(position, source)
          sourceSelectedListener onSourceSelected event
        }
      case _ =>
        Log error s"unknown row: $row"
    }
  }

  override def getItemCount = sourceAccessor.length
}

trait OnSourceSelectedListener {
  def onSourceSelected(event: SourceSelectedEvent): Unit
}

case class SourceSelectedEvent (position: Int, source: UnreadSource){
  def dump: String = s"sourceId:${source.id}, position:$position"
}
