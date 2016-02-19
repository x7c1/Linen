package x7c1.wheat.modern.observer.recycler

import java.lang.Math.abs

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SimpleOnItemTouchListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.{GestureDetector, MotionEvent, View}
import x7c1.wheat.macros.logger.Log

object VerticalDragDetector {
  def create(
    context: Context,
    flingVelocityThreshold: Int,
    flingDistanceThreshold: Int,
    onFling: FlingEvent => Unit,
    onDragStart: () => Unit,
    onDrag: DragEvent => Unit,
    onDragStopped: DragStoppedEvent => Unit
  ): VerticalDragDetector[DragStoppedEvent] = {

    val factory = new DragStoppedEventFactory[DragStoppedEvent] {
      override def createEvent(distance0: Float, direction0: DragDirection) = {
        new DragStoppedEvent {
          override def distance: Float = distance0
          override def direction: DragDirection = direction0
        }
      }
    }
    new VerticalDragDetector[DragStoppedEvent](
      context = context,
      stoppedEventFactory = factory,
      flingVelocityThreshold = flingVelocityThreshold,
      flingDistanceThreshold = flingDistanceThreshold,
      onFlingListener = onFling,
      onDragStart = onDragStart,
      onDrag = onDrag,
      onDragStopped = onDragStopped
    )
  }
}

class VerticalDragDetector[A <: DragStoppedEvent] private (
  context: Context,
  stoppedEventFactory: DragStoppedEventFactory[A],
//  onTouch: OnTouchListener,

  flingVelocityThreshold: Int,
  flingDistanceThreshold: Int,
  onFlingListener: FlingEvent => Unit,
  onDragStart: () => Unit,
  onDrag: DragEvent => Unit,
  onDragStopped: A => Unit
) extends SimpleOnItemTouchListener{

  private val detector = new GestureDetector(context, new VerticalFilter)
  private val flingDetector = new GestureDetector(context, new FlingFilter)
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
          distance = e.getRawY - start
          event = stoppedEventFactory.createEvent(distance, dir)
        } yield {
          onDragStopped(event)
        }
      case _ if direction.nonEmpty =>
        listenerToDrag.onTouch(rv, e)
      case _ =>
    }
    flingDetector.onTouchEvent(e)

    if (!(previous contains e.getRawY)){
      direction = previous map (e.getRawY - _) flatMap DragDirection.create
      previous = Some(e.getRawY)
    }
  }
  override def onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = {
    val isVertical = detector onTouchEvent e
    flingDetector.onTouchEvent(e)

    isVertical match {
      case false =>
        listenerToDrag updateCurrentPosition e.getRawY

      case _ if direction.nonEmpty =>
        listenerToDrag.onTouch(rv, e)
      case _ =>
    }
    isVertical
  }
  private class FlingFilter extends SimpleOnGestureListener {
    override def onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = {

      val distance = for {
        y1 <- Option(e1) map (_.getRawY)
        y2 <- Option(e2) map (_.getRawY)
      } yield {
        abs(y2 - y1)
      }

//      Log info s"$velocityY, $flingVelocityThreshold, $distance, ${Option(e1).map(_.getRawY)}, ${Option(e2).map(_.getRawY)}"

      val shouldFling =
        (abs(velocityY) > flingVelocityThreshold) &&
        (distance exists (_ > flingDistanceThreshold))

      if (shouldFling){
        DragDirection create velocityY foreach { direction =>
          onFlingListener(FlingEvent(direction))
        }
      }

      false
    }
  }
  private class VerticalFilter extends SimpleOnGestureListener {
    private var horizontalCount = 0
    private var verticalCount = 0

    override def onScroll(
      e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {

      val isVertical = abs(distanceY) > abs(2 * distanceX)

      if (isVertical && verticalCount == 0) {
        onDragStart()
      }

      if (isVertical){
        verticalCount += 1
      } else {
        horizontalCount += 1
      }
      Log info s"h:$horizontalCount, v:$verticalCount"
      val accepted = verticalCount > 0
      accepted
    }
    override def onDown(e: MotionEvent): Boolean = {
      horizontalCount = 0
      verticalCount = 0
      startPosition = Some(e.getRawY)
      false
    }
  }
  private class OnTouchToDrag extends OnTouchListener {
    private var currentPosition = Some(0F)

    def updateCurrentPosition(y: Float) = {
      currentPosition = Some(y)
    }
    override def onTouch(v: View, event: MotionEvent): Boolean = {
      currentPosition foreach { y =>
        val diff = event.getRawY - y
        onDrag apply DragEvent(diff)
      }
      updateCurrentPosition(event.getRawY)
      true
    }
  }
}
