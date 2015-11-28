package x7c1.wheat.modern.tasks

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.{LinearLayoutManager, LinearSmoothScroller, RecyclerView}
import android.util.DisplayMetrics
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish, UiThreadTask}


object ScrollerTasks {
  def apply(recyclerView: RecyclerView): ScrollerTasks = {
    new ScrollerTasks(recyclerView)
  }
}

class ScrollerTasks private (recyclerView: RecyclerView) {

  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }

  def notifyAndScroll(position: Int, length: Int)(done: OnFinish): CallbackTask[Unit] =
    for {
      ui <- task {
        Log debug s"[init] position:$position, length:$length"
        UiThreadTask from recyclerView
      }
      _ <- ui { view =>
        val current = layoutManager.findFirstCompletelyVisibleItemPosition()
        val base = if(current == position) -1 else 0
        view.getAdapter.notifyItemRangeInserted(position + base, length)
      }
      _ <- fastScrollTo(position)(done)
    } yield ()

  def fastScrollTo(position: Int)(done: OnFinish): CallbackTask[Unit] =
    for {
      ui <- task {
        Log debug s"[init] position:$position"
        UiThreadTask from recyclerView
      }
      _ <- ui { _ =>
        val current = layoutManager.findFirstCompletelyVisibleItemPosition()
        val diff = current - position
        val space = if (diff < 0) -1 else if(diff > 0) 1 else 0
        layoutManager.scrollToPositionWithOffset(position + space, 0)
      }
      _ <- ui { view =>
        val scroller = new SmoothScroller(
          view.getContext, timePerInch = 125F, layoutManager,
          done.by[ScrollerStopEvent]
        )
        scroller setTargetPosition position
        layoutManager startSmoothScroll scroller
      }
    } yield ()

  def scrollTo(position: Int)(done: OnFinish): CallbackTask[Unit] =
    for {
      ui <- task {
        UiThreadTask from recyclerView
      }
      scroller <- task {
        new SmoothScroller(
          recyclerView.getContext, timePerInch = 45F, layoutManager,
          done.by[ScrollerStopEvent] )
      }
      _ <- ui { _ =>
        scroller setTargetPosition position
        layoutManager startSmoothScroll scroller
      }
    } yield ()
}

class ScrollerStopEvent

class SmoothScroller(
  context: Context,
  timePerInch: Float,
  layoutManager: LinearLayoutManager,
  onFinish: ScrollerStopEvent => Unit) extends LinearSmoothScroller(context: Context) {

  override def computeScrollVectorForPosition(i: Int): PointF = {
    layoutManager.computeScrollVectorForPosition(i)
  }
  override def getVerticalSnapPreference: Int = {
    LinearSmoothScroller.SNAP_TO_START
  }
  override def onStart() = {
    Log debug s"[init] $timePerInch"
  }
  override def onStop(): Unit = {
    Log debug s"[done] $timePerInch"
    onFinish(new ScrollerStopEvent)
  }
  override def calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float = {
    timePerInch / displayMetrics.densityDpi
  }
}
