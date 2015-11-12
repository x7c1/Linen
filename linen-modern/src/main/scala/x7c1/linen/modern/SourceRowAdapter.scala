package x7c1.linen.modern

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.SourceRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class SourceRowAdapter(
  sourceAccessor: SourceAccessor,
  sourceSelectedListener: OnSourceSelectedListener,
  viewHolderProvider: ViewHolderProvider[SourceRow]) extends Adapter[SourceRow]{

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }

  override def onBindViewHolder(holder: SourceRow, position: Int) = {
    val source = sourceAccessor.get(position)
    holder.title.text = source.title
    holder.description.text = source.description
    holder.itemView onClick { view =>
      val event = SourceSelectedEvent(source, position)
      sourceSelectedListener onSourceSelected event
    }
  }

  override def getItemCount = sourceAccessor.get.length
}

trait OnSourceSelectedListener {
  def onSourceSelected(event: SourceSelectedEvent): Unit
}

class PaneController extends OnSourceSelectedListener {

  /*
  def execute
  def container
  def entriesArea
  def sourcesArea
  */

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info event
    /*
    val select = container.focusOn(entriesArea) then {
      sourcesArea select event.position
    }
    val load = entriesArea.displayOrLoad(event.sourceId)

    execute.inParallel(select and load)
    */
  }

}

class ScrollObserver (recyclerView: RecyclerView){

  def init() = {
    recyclerView onScroll { event =>
//      Log debug event
    }
  }

}
