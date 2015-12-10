package x7c1.linen.modern.action.observer

import android.content.Context
import android.view.View.OnTouchListener
import android.view.{MotionEvent, View}
import x7c1.linen.modern.action.{Actions, SourceSkippedEvent}
import x7c1.wheat.macros.logger.Log

object SourceSkippedDetector {
  def createListener(
    context: Context,
    skippedEventFactory: SkippedEventFactory[SourceSkippedEvent],
    onSkippedListener: OnItemSkippedListener): OnTouchListener = {
    /*
    val detector = new GestureDetector(
      context,
      new GestureListener
    )
    */
    new OnTouchListener {
      override def onTouch(v: View, event: MotionEvent): Boolean = {
        event.getAction match {
          case MotionEvent.ACTION_DOWN =>
            Log error "down"
            false
          case MotionEvent.ACTION_UP | MotionEvent.ACTION_CANCEL =>
            Log error "up"
            skippedEventFactory.create() foreach onSkippedListener.onSkipped
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

trait SkippedEventFactory[A <: ItemSkippedEvent]{
  def create(): Option[A]
}

trait OnItemSkippedListener {
  def onSkipped(event: SourceSkippedEvent): Unit
}

class SourceSkippedObserver(actions: Actions) extends OnItemSkippedListener {
  override def onSkipped(event: SourceSkippedEvent) = {
    val sync = for {
      _ <- actions.sourceArea onSourceSkipped event
      _ <- actions.entryArea onSourceSkipped event
      _ <- actions.detailArea onSourceSkipped event
      _ <- actions.prefetcher onSourceSkipped event
    } yield ()

    Seq(sync) foreach CallbackTaskRunner.runAsync { Log error _.toString }
  }
}
