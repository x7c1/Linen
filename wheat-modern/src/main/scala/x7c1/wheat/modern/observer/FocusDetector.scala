package x7c1.wheat.modern.observer

import android.support.v7.widget.RecyclerView
import android.view.GestureDetector.OnGestureListener
import android.view.View.OnTouchListener
import android.view.{GestureDetector, MotionEvent, View}

object FocusDetector {
  def createListener[A <: ItemFocusedEvent](
    recyclerView: RecyclerView,
    getPosition: () => Int,
    focusedEventFactory: FocusedEventFactory[A],
    onFocused: OnItemFocusedListener[A]): OnTouchListener = {

    val notifier = new FocusedItemNotifier(getPosition, focusedEventFactory, onFocused)
    val observer = new VerticalTouchScrollObserver(recyclerView, notifier)
    val detector = new GestureDetector(
      recyclerView.getContext,
      new GestureFilter(observer)
    )
    new OnTouchListener {
      override def onTouch(v: View, event: MotionEvent): Boolean = {
        detector onTouchEvent event
      }
    }
  }
}

trait ItemFocusedEvent {
  require(position > -1, "position must be non negative")
  def position: Int
}

trait FocusedEventFactory[A <: ItemFocusedEvent]{
  def createAt(position: Int): Option[A]
}

trait OnItemFocusedListener[A <: ItemFocusedEvent] {
  def onFocused(event: A): Unit
}

trait OnScrollStoppedListener {
  def onScrollStopped(e: ScrollStoppedEvent): Unit
}

case class ScrollStoppedEvent(offset: Int)

private class FocusedItemNotifier[A <: ItemFocusedEvent](
  getPosition: () => Int,
  focusedEventFactory: FocusedEventFactory[A],
  onFocusedListener: OnItemFocusedListener[A] ) extends OnScrollStoppedListener {

  override def onScrollStopped(e: ScrollStoppedEvent): Unit = {
    val position = getPosition()
    if (position > -1){
      focusedEventFactory createAt position foreach onFocusedListener.onFocused
    }
  }
}

private class GestureFilter(
  observer: TouchScrollObserver ) extends OnGestureListener {

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

    observer.touch()
    false
  }
  override def onDown(e: MotionEvent): Boolean = {
    false
  }
}
