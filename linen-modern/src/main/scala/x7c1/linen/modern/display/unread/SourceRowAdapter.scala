package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{UnreadSourceRowFooter, UnreadSourceRow, UnreadSourceRowItem}
import x7c1.linen.modern.accessor.unread.{UnreadSource, UnreadSourceAccessor}
import x7c1.linen.modern.init.unread.SourceListProviders
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
    sourceAccessor.bindViewHolder(row, position){
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
  override def getItemViewType(position: Int) = viewTypeAt(position)

  override def getItemCount = sourceAccessor.length

  private lazy val viewTypeAt = providers createViewTyper sourceAccessor
}

trait OnSourceSelectedListener {
  def onSourceSelected(event: SourceSelectedEvent): Unit
}

case class SourceSelectedEvent (position: Int, source: UnreadSource){
  def dump: String = s"sourceId:${source.id}, position:$position"
}
