package x7c1.linen.modern

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import android.widget.Scroller
import x7c1.linen.glue.res.layout.SourceRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.kinds.CallbackTask
import x7c1.wheat.modern.kinds.CallbackTask.taskOf

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}


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

class PaneController(
  container: PaneContainer) extends OnSourceSelectedListener {

  def sourcesArea = new SourcesArea(displayPosition = 0)

  def entriesArea = new EntriesArea(displayPosition = 864)

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info event

    val focus = for {
      _ <- taskOf(container scrollTo entriesArea)
      _ <- taskOf(sourcesArea scrollTo event.position)
    } yield {
      Log info s"[done] focus source-${event.sourceId}"
    }
    val show = for {
      _ <- taskOf(entriesArea displayOrLoad event.sourceId)
    } yield {
      Log info s"[done] show source-${event.sourceId}"
    }
    Seq(focus, show) foreach runAsync
  }

  def runAsync[A](task: CallbackTask[A]) = {
    Task(task()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }

}

class PaneContainer(view: ViewGroup) {
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

    override def run(): Unit = if (!scroller.isFinished) {
      val more = scroller.computeScrollOffset()
      val current = scroller.getCurrX
      view.scrollTo(current, 0)

      if (more){
        view.post(this)
      } else {
        Log info s"[done] current:$current"
        onFinish(new ContainerFocusedEvent)
      }
    }
  }
}

class ContainerFocusedEvent

class ScrollObserver (recyclerView: RecyclerView){

  def init() = {
    recyclerView onScroll { event =>
    }
  }

}
