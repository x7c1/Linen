package x7c1.linen.modern.action.observer

import java.lang.Math.min
import java.lang.System.currentTimeMillis
import java.util.{Timer, TimerTask}

import android.content.Context
import android.view.View.OnTouchListener
import android.view.{MotionEvent, View}

object SourceSkippedDetector {
  def createListener[A <: ItemSkippedEvent, B <: SkipDoneEvent](
    context: Context,
    getCurrentPosition: () => Option[Int],
    getNextPosition: () => Option[Int],
    skippedEventFactory: ItemSkippedEventFactory[A],
    skipDoneEventFactory: SkipDoneEventFactory[B],
    onSkippedListener: OnItemSkippedListener[A],
    onSkipDoneListener: OnSkipDoneListener[B]): OnTouchListener = {

    /*
    val detector = new GestureDetector(
      context,
      new GestureListener
    )
    */

    new OnTouchListener {

      val timer = new Timer()
      val maxInterval = 1500L

      var task: Option[TimerTask] = None
      var previous: Option[Long] = None
      var interval = 500L

      private def createTimerTask = new TimerTask {
        override def run(): Unit = for {
          next <- getNextPosition()
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
              interval = min(maxInterval, currentTimeMillis() - msec)
            }
            previous = Some(currentTimeMillis())
            task foreach { timer.schedule(_, interval, interval) }
            false
          case MotionEvent.ACTION_UP | MotionEvent.ACTION_CANCEL =>
            task foreach {_.cancel()}
            previous foreach { msec =>
              val elapsed = currentTimeMillis() - msec
              if (elapsed < interval) for {
                next <- getNextPosition()
                skipped <- skippedEventFactory createAt next
                done <- skipDoneEventFactory createAt next
              } yield {
                onSkippedListener onSkipped skipped
                onSkipDoneListener onSkipDone done
              } else for {
                current <- getCurrentPosition()
                done <- skipDoneEventFactory createAt current
              } yield {
                onSkipDoneListener onSkipDone done
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
trait SkipDoneEvent {
  require(currentPosition > -1, "must be non negative")
  def currentPosition: Int
}
trait ItemSkippedEventFactory[A <: ItemSkippedEvent]{
  def createAt(nextPosition: Int): Option[A]
}
trait SkipDoneEventFactory[A <: SkipDoneEvent]{
  def createAt(donePosition: Int): Option[A]
}
trait OnItemSkippedListener[A <: ItemSkippedEvent] {
  def onSkipped(event: A): Unit
}
trait OnSkipDoneListener[A <: SkipDoneEvent]{
  def onSkipDone(event: A): Unit
}
