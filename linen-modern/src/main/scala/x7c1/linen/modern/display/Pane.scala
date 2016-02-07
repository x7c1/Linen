package x7c1.linen.modern.display

import java.lang.Math.{abs, max, min}

import android.content.Context
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Scroller
import x7c1.linen.modern.action.Actions
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.observer.recycler.{HorizontalDragDetector, DragDirection, DragStoppedEvent, DragStoppedEventFactory}
import x7c1.wheat.modern.tasks.ScrollerTasks

trait Pane {
  def displayPosition: Int

  protected def scrollerTasks: ScrollerTasks

  def fastScrollTo(position: Int): CallbackTask[Unit] = {
    scrollerTasks fastScrollTo position
  }
  def scrollTo(position: Int): CallbackTask[Unit] = {
    scrollerTasks scrollTo position
  }
  def skipTo(position: Int): CallbackTask[Unit] = {
    scrollerTasks skipTo position
  }
}

class PaneContainer(
  view: ViewGroup,
  displayWidth: Int,
  val sourceArea: SourceArea,
  val entryArea: EntryArea,
  val entryDetailArea: EntryDetailArea
) {
  private lazy val scroller = new Scroller(view.getContext)

  private lazy val width = {
    val length = view.getChildCount
    val children = 0 to (length - 1) map view.getChildAt
    children.foldLeft(0){_ + _.getWidth} - displayWidth
  }
  def scrollBy(x: Int): Unit = {
    scroller forceFinished true

    val current = view.getScrollX
    val diff = min(width - current, max(x, -current))
    view.scrollBy(diff, 0)
  }
  def scrollTo(pane: Pane): CallbackTask[Unit] = task of {
    (done: OnFinish) => for {
      _ <- task {
        val current = view.getScrollX
        val dx = pane.displayPosition - current
        val duration = 500

        scroller.startScroll(current, 0, dx, 0, duration)
        view post new ContainerScroller(done)
      }
    } yield ()
  }
  def findPreviousPane(): Option[Pane] = {
    val current = view.getScrollX
    panes.reverse find (_.displayPosition < current)
  }
  def findCurrentPane(): Option[Pane] = {
    val current = view.getScrollX
    panes.reverse find (_.displayPosition == current)
  }
  private def panes = Seq(
    sourceArea,
    entryArea,
    entryDetailArea
  )
  private class ContainerScroller(done: OnFinish) extends Runnable {
    override def run(): Unit = {
      val more = scroller.computeScrollOffset()
      val current = scroller.getCurrX
      if (more){
        view.scrollTo(current, 0)
        view.post(this)
      } else {
        done.evaluate()
      }
    }
  }
}

class PaneDragStoppedEvent (
  override val distance: Float,
  override val direction: DragDirection,
  val from: Pane,
  thresholdPixel: Int ) extends DragStoppedEvent {

  private def back = {
    val dir = DragDirection create distance
    !(dir contains direction)
  }
  private def near = abs(distance) < thresholdPixel

  def rejected = back || near
}

class PaneDragStoppedEventFactory(from: Pane, thresholdPixel: Int)
  extends DragStoppedEventFactory[PaneDragStoppedEvent] {

  override def createEvent(distance: Float, direction: DragDirection) = {
    new PaneDragStoppedEvent(distance, direction, from, thresholdPixel)
  }
}

object PaneDragDetector {
  def create(
    context: Context,
    from: Pane,
    actions: Actions,
    onTouch: OnTouchListener): HorizontalDragDetector[PaneDragStoppedEvent] = {

    val threshold = {
      val dp = 15
      val metrics = context.getResources.getDisplayMetrics
      TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, metrics)
    }
    new HorizontalDragDetector(
      context = context,
      stoppedEventFactory = new PaneDragStoppedEventFactory(from, threshold.toInt),
      onTouch = onTouch,
      onDrag = actions.container.onPaneDragging,
      onDragStopped = actions.container.onPaneDragStopped
    )
  }
}
