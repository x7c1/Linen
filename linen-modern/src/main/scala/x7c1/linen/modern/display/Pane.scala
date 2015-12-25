package x7c1.linen.modern.display

import java.lang.Math.max

import android.view.{MotionEvent, View, ViewGroup}
import android.widget.Scroller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.observer.AppendableOnTouch
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

case class PaneDragEvent (
  from: PaneLabel,
  distanceX: Float
)
case class PaneDragStoppedEvent (
  from: PaneLabel,
  direction: Int
)

class OnTouchToScrollPane(
  from: PaneLabel,
  onDrag: PaneDragEvent => Unit) extends AppendableOnTouch {

  private var previousPosition = Some(0F)

  def updateCurrentPosition(x: Float) = {
    previousPosition = Some(x)
  }

  override def onTouch(v: View, event: MotionEvent): Boolean = {
    previousPosition foreach { x =>
      val diff = x - event.getRawX
      onDrag apply PaneDragEvent(from, diff)
    }
    previousPosition = Some(event.getRawX)
    true
  }

}
