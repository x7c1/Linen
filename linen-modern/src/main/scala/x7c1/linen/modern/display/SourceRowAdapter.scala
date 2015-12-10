package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView.Adapter
import android.view.{View, ViewGroup}
import x7c1.linen.glue.res.layout.SourceRow
import x7c1.linen.modern.accessor.{SourceAccessor, SourcePrefetched, SourceStateAccessor}
import x7c1.linen.modern.struct.Source
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._


class SourceRowAdapter(
  sourceAccessor: SourceAccessor,
  sourceStateAccessor: SourceStateAccessor,
  sourceSelectedListener: OnSourceSelectedListener,
  viewHolderProvider: ViewHolderProvider[SourceRow]) extends Adapter[SourceRow]{

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }

  override def onBindViewHolder(holder: SourceRow, position: Int) = {
    sourceAccessor.findAt(position) foreach { source =>
      holder.title.text = source.title
      holder.description.text = source.description
      holder.itemView onClick { view =>
        val event = SourceSelectedEvent(position, source)
        sourceSelectedListener onSourceSelected event
      }
      holder.statePrefetched.setVisibility(View.GONE)
      holder.stateUnloaded.setVisibility(View.GONE)

      sourceStateAccessor.findState(source.id) match {
        case Some(SourcePrefetched) =>
          holder.statePrefetched.setVisibility(View.VISIBLE)
        case _ =>
          holder.stateUnloaded.setVisibility(View.VISIBLE)
      }
    }
  }

  override def getItemCount = sourceAccessor.length
}

trait OnSourceSelectedListener {
  def onSourceSelected(event: SourceSelectedEvent): Unit
}

case class SourceSelectedEvent (position: Int, source: Source){
  def dump: String = s"sourceId:${source.id}, position:$position"
}
