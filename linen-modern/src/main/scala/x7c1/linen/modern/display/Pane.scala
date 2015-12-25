package x7c1.linen.modern.display

import java.lang.Math.max

import android.content.Context
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Scroller
import x7c1.linen.modern.action.Actions
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.observer.recycler.{DragDetector, DragDirection, DragStoppedEvent, DragStoppedEventFactory}
import x7c1.wheat.modern.tasks.ScrollerTasks

trait Pane {
  def displayPosition: Int

  protected def scrollerTasks: ScrollerTasks

  def fastScrollTo(position: Int): CallbackTask[Unit] = {
    scrollerTasks fastScrollTo position
  }
  def scrollTo(position: Int): CallbackTask[Unit] = {
    scrollerTasks scrollTo position
  }
  def skipTo(position: Int): CallbackTask[Unit] = {
    scrollerTasks skipTo position
  }
}

class PaneContainer(view: ViewGroup) {
  private lazy val scroller = new Scroller(view.getContext)

  def scrollBy(x: Int): Unit = {
    view.scrollBy(max(x, -view.getScrollX), 0)
  }

  def scrollTo(pane: Pane): CallbackTask[Unit] = task of {
    (done: OnFinish) => for {
      _ <- task {
        val current = view.getScrollX
        val dx = pane.displayPosition - current
        val duration = 500

        Log info s"[init] current:$current, dx:$dx"
        scroller.startScroll(current, 0, dx, 0, duration)

        view post new ContainerScroller(done)
      }
    } yield ()
  }
  private class ContainerScroller(done: OnFinish) extends Runnable {

    override def run(): Unit = {
      val more = scroller.computeScrollOffset()
      val current = scroller.getCurrX
      if (more){
        view.scrollTo(current, 0)
        view.post(this)
      } else {
        Log info s"[done] current:$current"
        done.evaluate()
      }
    }
  }
}

case class PaneDragStoppedEvent (
  from: PaneLabel,
  override val direction: DragDirection ) extends DragStoppedEvent

class PaneDragStoppedEventFactory(from: PaneLabel)
  extends DragStoppedEventFactory[PaneDragStoppedEvent] {

  override def createEvent(direction: DragDirection) = {
    PaneDragStoppedEvent(from, direction)
  }
}

object PaneDragDetector {
  def create(
    context: Context,
    label: PaneLabel,
    actions: Actions,
    onTouch: OnTouchListener): DragDetector[PaneDragStoppedEvent] = {

    new DragDetector(
      context = context,
      stoppedEventFactory = new PaneDragStoppedEventFactory(label),
      onTouch = onTouch,
      onDrag = actions.container.onPaneDragging,
      onDragStopped = actions.container.onPaneDragStopped
    )
  }
}
