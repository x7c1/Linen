package x7c1.wheat.modern.observer.recycler

import java.lang.Math.abs

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SimpleOnItemTouchListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.{GestureDetector, MotionEvent, View}


class HorizontalDragDetector[A <: DragStoppedEvent](
  context: Context,
  stoppedEventFactory: DragStoppedEventFactory[A],
  onTouch: OnTouchListener,
  onDrag: DragEvent => Unit,
  onDragStopped: A => Unit) extends SimpleOnItemTouchListener{

  private val detector = new GestureDetector(context, new HorizontalFilter)
  private val listenerToDrag = new OnTouchToDrag

  private var startPosition: Option[Float] = None

  override def onTouchEvent(rv: RecyclerView, e: MotionEvent): Unit = {
    e.getAction match {
      case MotionEvent.ACTION_UP =>
        for {
          start <- startPosition
          distance = e.getRawX - start
          dir <- DragDirection create distance
          event = stoppedEventFactory.createEvent(distance, dir)
        } yield {
          onDragStopped(event)
        }
      case _ => listenerToDrag.onTouch(rv, e)
    }
  }
  override def onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = {
    val isHorizontal = detector onTouchEvent e
    if (isHorizontal){
      listenerToDrag.onTouch(rv, e)
    } else {
      onTouch.onTouch(rv, e)
      listenerToDrag updateCurrentPosition e.getRawX
    }
    isHorizontal
  }
  private class HorizontalFilter extends SimpleOnGestureListener {
    private var horizontalCount = 0
    private var verticalCount = 0

    override def onScroll(
      e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {

      val isHorizontal = abs(distanceX) > abs(2 * distanceY)
      if (isHorizontal){
        horizontalCount += 1
      } else {
        verticalCount += 1
      }
      val accepted = horizontalCount > 0 && verticalCount < 3
      accepted
    }
    override def onDown(e: MotionEvent): Boolean = {
      horizontalCount = 0
      verticalCount = 0
      startPosition = Some(e.getRawX)
      false
    }
  }
  private class OnTouchToDrag extends OnTouchListener {
    private var currentPosition = Some(0F)

    def updateCurrentPosition(x: Float) = {
      currentPosition = Some(x)
    }
    override def onTouch(v: View, event: MotionEvent): Boolean = {
      currentPosition foreach { x =>
        val diff = event.getRawX - x
        onDrag apply DragEvent(diff)
      }
      updateCurrentPosition(event.getRawX)
      true
    }
  }
}

case class DragEvent(distance: Float)

trait DragStoppedEvent {
  def direction: DragDirection
}
trait DragStoppedEventFactory[A <: DragStoppedEvent]{
  def createEvent(distance: Float, direction: DragDirection): A
}

