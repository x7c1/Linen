package x7c1.linen.modern.display.unread

import java.lang.Math.{abs, max, min}

import android.animation.Animator.AnimatorListener
import android.animation.{ObjectAnimator, Animator, AnimatorSet}
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Scroller
import x7c1.linen.modern.action.Actions
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.observer.recycler.{DragDirection, DragStoppedEvent, DragStoppedEventFactory, HorizontalDragDetector}
import x7c1.wheat.modern.tasks.ScrollerTasks

trait Pane {
  def displayPosition: Int

  protected def scrollerTasks: ScrollerTasks

  protected def recyclerView: RecyclerView

  def fastScrollTo(position: Int): CallbackTask[Unit] = {
    scrollerTasks fastScrollTo position
  }
  def scrollTo(position: Int, timePerInch: Float = 45F): CallbackTask[Unit] = {
    scrollerTasks.scrollTo(position, timePerInch)
  }
  def skipTo(position: Int): CallbackTask[Unit] = {
    scrollerTasks skipTo position
  }
  def fadeOutAnimator: ObjectAnimator = {
    ObjectAnimator.ofFloat(recyclerView, "alpha", 1.0F, 0F)
  }
  def fadeInAnimator: ObjectAnimator = {
    ObjectAnimator.ofFloat(recyclerView, "alpha", 0F, 1.0F)
  }
}

class PaneContainer(
  view: ViewGroup,
  displayWidth: Int,
  val sourceArea: SourceArea,
  val outlineArea: OutlineArea,
  val detailArea: DetailArea
) {
  private lazy val scroller = new Scroller(view.getContext)

  private lazy val width = {
    val length = view.getChildCount
    val children = 0 until length map view.getChildAt
    children.foldLeft(0){_ + _.getWidth} - displayWidth
  }
  def scrollBy(x: Int): Unit = {
    scroller forceFinished true

    val current = view.getScrollX
    val diff = min(width - current, max(x, -current))
    view.scrollBy(diff, 0)
  }
  def skipTo(pane: Pane): CallbackTask[Unit] = task {
    view.scrollTo(pane.displayPosition, 0)
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
  def fadeOut(): CallbackTask[Unit] = task of { (done: OnFinish) =>
    val set = new AnimatorSet()
    set addListener new AnimatorListener {
      override def onAnimationEnd(animation: Animator): Unit = done.evaluate()
      override def onAnimationRepeat(animation: Animator): Unit = {}
      override def onAnimationStart(animation: Animator): Unit = {}
      override def onAnimationCancel(animation: Animator): Unit = {}
    }
    set setDuration 100
    set.playTogether(
      sourceArea.fadeOutAnimator,
      outlineArea.fadeOutAnimator,
      detailArea.fadeOutAnimator
    )
    set.start()
  }
  def fadeIn(): CallbackTask[Unit] = task of { (done: OnFinish) =>
    val set = new AnimatorSet()
    set addListener new AnimatorListener {
      override def onAnimationEnd(animation: Animator): Unit = done.evaluate()
      override def onAnimationRepeat(animation: Animator): Unit = {}
      override def onAnimationStart(animation: Animator): Unit = {}
      override def onAnimationCancel(animation: Animator): Unit = {}
    }
    set setDuration 100
    set.playTogether(
      sourceArea.fadeInAnimator,
      outlineArea.fadeInAnimator,
      detailArea.fadeInAnimator
    )
    set.start()
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
    outlineArea,
    detailArea
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
