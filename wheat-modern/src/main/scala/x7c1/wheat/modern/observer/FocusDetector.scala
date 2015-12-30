package x7c1.wheat.modern.observer

import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.{GestureDetector, MotionEvent}

object FocusDetector {
  def createListener[A <: ItemFocusedEvent](
    recyclerView: RecyclerView,
    getPosition: () => Int,
    focusedEventFactory: FocusedEventFactory[A],
    onFocused: OnItemFocusedListener[A]): AppendableOnTouch = {

    val notifier = new FocusedItemNotifier(getPosition, focusedEventFactory, onFocused)
    val observer = new VerticalTouchScrollObserver(recyclerView, notifier)
    val detector = new GestureDetector(
      recyclerView.getContext,
      new GestureFilter(observer)
    )
    AppendableOnTouch((_, e) => detector onTouchEvent e)
  }
  def forLinearLayoutManager[A <: ItemFocusedEvent](
    recyclerView: RecyclerView,
    focusedEventFactory: FocusedEventFactory[A],
    onFocused: OnItemFocusedListener[A]): AppendableOnTouch = {

    val layoutManager = recyclerView.getLayoutManager match {
      case x: LinearLayoutManager => x
      case _ =>
        throw new IllegalArgumentException("invalid type of LayoutManager")
    }
    val getPosition = () => {
      layoutManager.findFirstCompletelyVisibleItemPosition() match {
        case n if n < 0 => layoutManager.findFirstVisibleItemPosition()
        case n => n
      }
    }
    createListener(
      recyclerView,
      getPosition,
      focusedEventFactory,
      onFocused
    )
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
      focusedEventFactory createAt
        position foreach onFocusedListener.onFocused
    }
  }
}

private class GestureFilter(
  observer: TouchScrollObserver ) extends SimpleOnGestureListener {

  override def onScroll(
    e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {

    observer.touch()
    false
  }
}
