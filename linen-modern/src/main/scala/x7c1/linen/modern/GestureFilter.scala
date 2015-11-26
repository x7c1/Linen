package x7c1.linen.modern

import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.GestureDetector.OnGestureListener
import android.view.{GestureDetector, MotionEvent}

class GestureFilter(
  recyclerView: RecyclerView,
  notifier: FocusedItemNotifier ) extends OnGestureListener {

  private lazy val observer = new VerticalScrollObserver(recyclerView, notifier)

  override def onSingleTapUp(e: MotionEvent): Boolean = {
    false
  }
  override def onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = {
    false
  }
  override def onShowPress(e: MotionEvent): Unit = {}

  override def onLongPress(e: MotionEvent): Unit = {}

  override def onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {
    observer.touch()
    false
  }
  override def onDown(e: MotionEvent): Boolean = {
    false
  }
}

object ItemFocusDetector {
  def createOnTouch(
    recyclerView: RecyclerView,
    layoutManager: LinearLayoutManager,
    onItemFocused: OnItemFocusedListener): (RecyclerView, MotionEvent) => Boolean = {

    val notifier = new FocusedItemNotifier(layoutManager, onItemFocused)
    val filter = new GestureFilter(recyclerView, notifier)
    val detector = new GestureDetector(recyclerView.getContext, filter)

    (_, event) => detector onTouchEvent event
  }
}

class FocusedItemNotifier(
  layoutManager: LinearLayoutManager,
  onItemFocusedListener: OnItemFocusedListener ) extends OnScrollStoppedListener {

  override def onScrollStopped(e: ScrollStoppedEvent): Unit = {
    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
    if (position > -1){
      onItemFocusedListener onItemFocused new ItemFocusedEvent(position)
    }
  }
}
