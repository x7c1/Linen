package x7c1.linen.modern

import android.support.v7.widget.RecyclerView.Adapter
import android.view.{View, ViewGroup}
import android.widget.Scroller
import x7c1.linen.glue.res.layout.SourceRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
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
    val source = sourceAccessor.get(position)
    holder.title.text = source.title
    holder.description.text = source.description
    holder.itemView onClick { view =>
      val event = SourceSelectedEvent(source, position)
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

  override def getItemCount = sourceAccessor.get.length
}

trait OnSourceSelectedListener {
  def onSourceSelected(event: SourceSelectedEvent): Unit
}

class PaneContainer(
  view: ViewGroup,
  val sourceArea: SourceArea,
  val entryArea: EntryArea ) {

  private lazy val scroller = new Scroller(view.getContext)

  def scrollTo(pane: Pane)(onFinish: ContainerFocusedEvent => Unit): Unit = {
    val current = view.getScrollX
    val dx = pane.displayPosition - current
    val duration = 350

    Log info s"[init] current:$current, dx:$dx"
    scroller.startScroll(current, 0, dx, 0, duration)

    view.post(new ContainerScroller(onFinish))
  }
  private class ContainerScroller(
    onFinish: ContainerFocusedEvent => Unit) extends Runnable {

    override def run(): Unit = {
      val more = scroller.computeScrollOffset()
      val current = scroller.getCurrX
      if (more){
        view.scrollTo(current, 0)
        view.post(this)
      } else {
        Log info s"[done] current:$current"
        onFinish(new ContainerFocusedEvent)
      }
    }
  }
}

class ContainerFocusedEvent
