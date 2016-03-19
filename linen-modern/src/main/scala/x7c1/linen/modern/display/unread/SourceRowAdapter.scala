package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{UnreadSourceRowFooter, UnreadSourceRow, UnreadSourceRowItem}
import x7c1.linen.modern.accessor.unread.UnreadSourceAccessor
import x7c1.linen.modern.init.unread.SourceListProviders
import x7c1.linen.modern.struct.UnreadSource
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class SourceRowAdapter(
  sourceAccessor: UnreadSourceAccessor,
  sourceSelectedListener: OnSourceSelectedListener,
  providers: SourceListProviders,
  footerHeight: => Int ) extends Adapter[UnreadSourceRow]{

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    providers.createViewHolder(parent, viewType)
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
      case (_, holder: UnreadSourceRowFooter) =>
        holder.itemView setLayoutParams {
          val params = row.itemView.getLayoutParams
          params.height = footerHeight
          params
        }
      case _ =>
        Log error s"unknown row: $row"
    }
  }
  override def getItemViewType(position: Int) = {
    val provider = position match {
      case x if x == sourceAccessor.length => providers.forFooter
      case x => providers.forItem
    }
    provider.layoutId
  }

  override def getItemCount = {
    // +1 to include footer
    sourceAccessor.length + 1
  }
}

trait OnSourceSelectedListener {
  def onSourceSelected(event: SourceSelectedEvent): Unit
}

case class SourceSelectedEvent (position: Int, source: UnreadSource){
  def dump: String = s"sourceId:${source.id}, position:$position"
}
