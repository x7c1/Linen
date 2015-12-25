package x7c1.linen.modern.display

import java.lang.Math.max

import android.content.Context
import android.view.GestureDetector.OnGestureListener
import android.view.{View, GestureDetector, MotionEvent, ViewGroup}
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

  def scrollBy(x: Int) = {
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
  original1: Option[MotionEvent],
  original2: MotionEvent,
  distanceX: Float,
  distanceY: Float
)
case class PaneDragStoppedEvent (
  from: PaneLabel,
  direction: Int,
  original: MotionEvent
)

object PaneFlingDetector {
  def createListener(
    context: Context, from: PaneLabel, onFlung: PaneDragEvent => Boolean) = {

    new OnTouchToScrollPane(context, from, onFlung)
  }
}

class OnTouchToScrollPane(
  context: Context,
  from: PaneLabel,
  onDrag: PaneDragEvent => Boolean) extends AppendableOnTouch {

  val detector = new GestureDetector(
    context,
    new Filter
  )

  def updateCurrentPosition(e: MotionEvent) = {
    previousDistanceX = Some(e.getRawX)
  }

  override def onTouch(v: View, event: MotionEvent): Boolean = {
    detector onTouchEvent event
  }

  private var previousDistanceX = Some(0F)

  private class Filter extends OnGestureListener {

    override def onSingleTapUp(e: MotionEvent): Boolean = {
      false
    }
    override def onFling(
      e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = {

      false
    }
    override def onShowPress(e: MotionEvent): Unit = {}

    override def onLongPress(e: MotionEvent): Unit = {}

    override def onScroll(
      e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {

      previousDistanceX foreach { prev =>
        val x = prev - e2.getRawX
        onDrag apply PaneDragEvent(from, Option(e1), e2, x, distanceY)
      }
      previousDistanceX = Some(e2.getRawX)

      true
    }
    override def onDown(e: MotionEvent): Boolean = {
      Log error s"${e.getRawX}"

      previousDistanceX = Some(e.getRawX)

      false
    }
  }

}
