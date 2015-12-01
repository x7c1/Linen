package x7c1.wheat.modern.observer

import android.support.v7.widget.RecyclerView
import android.view.GestureDetector.OnGestureListener
import android.view.View.OnTouchListener
import android.view.{GestureDetector, MotionEvent, View}

object FocusDetector {
  def create(
    recyclerView: RecyclerView,
    getPosition: () => Int,
    onItemFocused: OnItemFocusedListener): OnTouchListener = {

    val notifier = new FocusedItemNotifier(getPosition, onItemFocused)
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

case class ItemFocusedEvent(position: Int){
  require(position > -1, "position must be non negative")

  def dump: String = s"position:$position"
}

trait OnItemFocusedListener {
  def onItemFocused(event: ItemFocusedEvent)
}

trait OnScrollStoppedListener {
  def onScrollStopped(e: ScrollStoppedEvent)
}

case class ScrollStoppedEvent(offset: Int)

private class FocusedItemNotifier(
  getPosition: () => Int,
  onItemFocusedListener: OnItemFocusedListener ) extends OnScrollStoppedListener {

  override def onScrollStopped(e: ScrollStoppedEvent): Unit = {
    val position = getPosition()
    if (position > -1){
      onItemFocusedListener onItemFocused new ItemFocusedEvent(position)
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
