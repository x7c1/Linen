package x7c1.wheat.modern.observer.recycler

import java.lang.Math.abs

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SimpleOnItemTouchListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.{GestureDetector, MotionEvent, View}
import x7c1.wheat.macros.logger.Log


class VerticalDragDetector[A <: DragStoppedEvent](
  context: Context
//  ,
//  stoppedEventFactory: DragStoppedEventFactory[A],
//  onTouch: OnTouchListener,
//  onDrag: DragEvent => Unit,
//  onDragStopped: A => Unit
) extends SimpleOnItemTouchListener{

  private val detector = new GestureDetector(context, new VerticalFilter)
  private val listenerToDrag = new OnTouchToDrag

  private var previous: Option[Float] = None
  private var direction: Option[DragDirection] = None

  private var startPosition: Option[Float] = None

  override def onTouchEvent(rv: RecyclerView, e: MotionEvent): Unit = {
    e.getAction match {
      case MotionEvent.ACTION_UP | MotionEvent.ACTION_CANCEL =>
        for {
          dir <- direction
          start <- startPosition
          distance = e.getRawX - start
//          event = stoppedEventFactory.createEvent(distance, dir)
        } yield {
          Log info s"$e"
//          onDragStopped(event)
        }
      case _ if direction.nonEmpty =>
        listenerToDrag.onTouch(rv, e)
      case _ =>
    }
    if (!(previous contains e.getRawX)){
      direction = previous map (e.getRawX - _) flatMap DragDirection.create
      previous = Some(e.getRawX)
    }
  }
  override def onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = {
    val isVertical = detector onTouchEvent e
    Log info s"$isVertical"
    isVertical match {
      case false =>
        listenerToDrag updateCurrentPosition e.getRawX

      case _ if direction.nonEmpty =>
        Log error s"$direction, $e"
        listenerToDrag.onTouch(rv, e)
      case _ =>
    }
    isVertical
  }
  private class VerticalFilter extends SimpleOnGestureListener {
    private var horizontalCount = 0
    private var verticalCount = 0

    override def onScroll(
      e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {

      val isVertical = abs(distanceY) > abs(2 * distanceX)
      if (isVertical){
        verticalCount += 1
      } else {
        horizontalCount += 1
      }
      Log info s"h:$horizontalCount, v:$verticalCount"
      val accepted = verticalCount > 0 && horizontalCount < 3
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
        Log info s"$event"
//        onDrag apply DragEvent(diff)
      }
      updateCurrentPosition(event.getRawX)
      true
    }
  }
}
