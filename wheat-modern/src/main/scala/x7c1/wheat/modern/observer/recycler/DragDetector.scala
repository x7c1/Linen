package x7c1.wheat.modern.observer.recycler

import java.lang.Math.abs

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SimpleOnItemTouchListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.{GestureDetector, MotionEvent, View}


class DragDetector[A <: DragStoppedEvent](
  context: Context,
  stoppedEventFactory: DragStoppedEventFactory[A],
  onTouch: OnTouchListener,
  onDrag: DragEvent => Unit,
  onDragStopped: A => Unit) extends SimpleOnItemTouchListener{

  private val detector = new GestureDetector(context, new HorizontalFilter)
  private var previous: Option[Float] = None
  private var direction: Option[DragDirection] = None
  private val listenerToScroll = new OnTouchToScroll

  override def onTouchEvent(rv: RecyclerView, e: MotionEvent): Unit = {
    e.getAction match {
      case MotionEvent.ACTION_UP => direction foreach { dir =>
        val event = stoppedEventFactory.createEvent(dir)
        onDragStopped(event)
      }
      case _ => listenerToScroll.onTouch(rv, e)
    }
    if (!(previous contains e.getRawX)){
      direction = previous map (e.getRawX - _) flatMap DragDirection.create
      previous = Some(e.getRawX)
    }
  }
  override def onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = {
    val isHorizontal = detector onTouchEvent e
    if (isHorizontal){
      listenerToScroll.onTouch(rv, e)
    } else {
      onTouch.onTouch(rv, e)
      listenerToScroll updateCurrentPosition e.getRawX
    }
    isHorizontal
  }
  private class HorizontalFilter extends SimpleOnGestureListener {
    private var horizontalCount = 0

    override def onScroll(
      e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {

      val isHorizontal = abs(distanceX) > abs(distanceY)
      if (isHorizontal){
        horizontalCount += 1
      }
      val accepted = horizontalCount > 5
      accepted
    }
    override def onDown(e: MotionEvent): Boolean = {
      horizontalCount = 0
      false
    }
  }
  private class OnTouchToScroll extends OnTouchListener {
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
  def createEvent(direction: DragDirection): A
}

