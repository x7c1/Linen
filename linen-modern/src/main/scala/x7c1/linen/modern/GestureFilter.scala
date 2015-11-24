package x7c1.linen.modern

import android.support.v7.widget.{RecyclerView, LinearLayoutManager}
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent

class GestureFilter(
  recyclerView: RecyclerView,
  notifier: FocusedSourceNotifier ) extends OnGestureListener {

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

class FocusedSourceNotifier(
  layoutManager: LinearLayoutManager,
  onSourceFocusedListener: OnSourceFocusedListener ) extends OnScrollStoppedListener {

  override def onScrollStopped(e: ScrollStoppedEvent): Unit = {
    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
    onSourceFocusedListener.onSourceFocused(new SourceFocusedEvent(position))
  }
}
