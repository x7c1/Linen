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

    new OnTouchListener {
      val timer = new Timer()

      val maxInterval = 2000L
      val minInterval = 150L
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
      private def elapsed(msec: Long): Long = {
        val multiplier = 1.2
        ((currentTimeMillis() - msec) * multiplier).toLong
      }
      override def onTouch(v: View, event: MotionEvent): Boolean = {
        event.getAction match {
          case MotionEvent.ACTION_DOWN =>
            task foreach {_.cancel()}
            task = Some(createTimerTask)
            previous foreach { msec =>
              interval = min(
                maxInterval,
                max(minInterval, elapsed(msec))
              )
            }
            previous = Some(currentTimeMillis())
            task foreach { timer.schedule(_, interval, interval) }
            false
          case MotionEvent.ACTION_UP | MotionEvent.ACTION_CANCEL =>
            task foreach {_.cancel()}
            previous foreach { msec =>
              if (elapsed(msec) < interval) for {
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
  import x7c1.wheat.modern.decorator.Imports._
  def createBy(manager: LinearLayoutManager): SkipPositionFinder =
    new SkipPositionFinder {
      override def findCurrent = {
        manager.firstCompletelyVisiblePosition orElse
          manager.firstVisiblePosition
      }
      override def findNext = {
        manager.firstVisiblePosition map (_ + 1)
      }
    }
}
