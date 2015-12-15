package x7c1.wheat.modern.observer

import java.lang.Math.{max, min}
import java.lang.System.currentTimeMillis
import java.util.{Timer, TimerTask}

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.View.OnTouchListener
import android.view.{MotionEvent, View}

object SkipDetector {
  def createListener[A <: ItemSkippedEvent, B <: SkipStoppedEvent](
    context: Context,
    positionFinder: SkipPositionFinder,
    skippedEventFactory: ItemSkippedEventFactory[A],
    skipDoneEventFactory: SkipStoppedEventFactory[B],
    onSkippedListener: OnItemSkippedListener[A],
    onSkipDoneListener: OnSkipStoppedListener[B]): OnTouchListener = {

    /*
    val detector = new GestureDetector(
      context,
      new GestureListener
    )
    */

    new OnTouchListener {

      val timer = new Timer()

      val maxInterval = 1500L
      val minInterval = 100L
      var interval = 500L

      var task: Option[TimerTask] = None
      var previous: Option[Long] = None

      private def createTimerTask = new TimerTask {
        override def run(): Unit = for {
          next <- positionFinder.findNext
          event <- skippedEventFactory createAt next
        } yield {
          onSkippedListener onSkipped event
        }
      }
      override def onTouch(v: View, event: MotionEvent): Boolean = {
        event.getAction match {
          case MotionEvent.ACTION_DOWN =>
            task = Some(createTimerTask)
            previous foreach { msec =>
              interval = min(
                maxInterval,
                max(minInterval, currentTimeMillis() - msec)
              )
            }
            previous = Some(currentTimeMillis())
            task foreach { timer.schedule(_, interval, interval) }
            false
          case MotionEvent.ACTION_UP | MotionEvent.ACTION_CANCEL =>
            task foreach {_.cancel()}
            previous foreach { msec =>
              val elapsed = currentTimeMillis() - msec
              if (elapsed < interval) for {
                next <- positionFinder.findNext
                skipped <- skippedEventFactory createAt next
                done <- skipDoneEventFactory createAt next
              } yield {
                onSkippedListener onSkipped skipped
                onSkipDoneListener onSkipStopped done
              } else for {
                current <- positionFinder.findCurrent
                done <- skipDoneEventFactory createAt current
              } yield {
                onSkipDoneListener onSkipStopped done
              }
            }
            true
          case _ =>
            false
        }
      }
    }
  }
  /*
  private class GestureListener extends GestureDetector.OnGestureListener {
    override def onSingleTapUp(e: MotionEvent): Boolean = {
      Log error "[init]"

      true
    }
    override def onFling(
      e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = {

      Log error "[init]"
      true
    }
    override def onShowPress(e: MotionEvent): Unit = {
      Log error "[init]"
    }
    override def onLongPress(e: MotionEvent): Unit = {
      Log error "[init]"
    }
    override def onScroll(
      e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {

      Log error "[init]"
      false
    }
    override def onDown(e: MotionEvent): Boolean = {
      Log error "[init]"
      false
    }
  }
  */
}

trait ItemSkippedEvent {
  require(nextPosition > -1, "must be non negative")
  def nextPosition: Int
}
trait SkipStoppedEvent {
  require(currentPosition > -1, "must be non negative")
  def currentPosition: Int
}
trait ItemSkippedEventFactory[A <: ItemSkippedEvent]{
  def createAt(nextPosition: Int): Option[A]
}
trait SkipStoppedEventFactory[A <: SkipStoppedEvent]{
  def createAt(donePosition: Int): Option[A]
}
trait OnItemSkippedListener[A <: ItemSkippedEvent] {
  def onSkipped(event: A): Unit
}
trait OnSkipStoppedListener[A <: SkipStoppedEvent]{
  def onSkipStopped(event: A): Unit
}

trait SkipPositionFinder {
  def findCurrent: Option[Int]
  def findNext: Option[Int]
}

object SkipPositionFinder {
  def createBy(manager: LinearLayoutManager): SkipPositionFinder =
    new SkipPositionFinder {
      override def findCurrent =
        manager.findFirstVisibleItemPosition() match {
          case x if x < 0 => None
          case x => Some(x)
        }

      override def findNext = Some {
        manager.findFirstVisibleItemPosition() + 1
      }
    }
}
