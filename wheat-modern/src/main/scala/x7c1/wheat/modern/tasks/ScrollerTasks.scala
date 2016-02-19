package x7c1.wheat.modern.tasks

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.{LinearLayoutManager, LinearSmoothScroller, RecyclerView}
import android.util.{DisplayMetrics, TypedValue}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}

object ScrollerTasks {
  def apply(
    recyclerView: RecyclerView,
    flowTimePerInch: Float,
    flowSpaceDip: Int): ScrollerTasks = {

    new ScrollerTasks(recyclerView, flowSpaceDip, flowTimePerInch)
  }
}

class ScrollerTasks private (
  recyclerView: RecyclerView, flowSpaceDip: Int, hastyTimePerInch: Float) {

  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
  private lazy val space: Int = {
    val metrics = recyclerView.getResources.getDisplayMetrics
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, flowSpaceDip, metrics).toInt
  }

  def fastScrollTo(position: Int): CallbackTask[Unit] = task of {
    (done: OnFinish) => for {
      ui <- task {
        UiThread via recyclerView
      }
      _ <- ui { _ =>
        recyclerView.stopScroll()

        val current = layoutManager.findFirstCompletelyVisibleItemPosition() match {
          case n if n < 0 =>
            layoutManager.findFirstVisibleItemPosition()
          case n => n
        }
        val diff = current - position
        val direction = if (diff < 0) 1 else if (diff > 0) -1 else 0
        layoutManager.scrollToPositionWithOffset(position, direction * space)
      }
      _ <- ui { view =>
        val scroller = new SmoothScroller(
          view.getContext, timePerInch = hastyTimePerInch, layoutManager,
          done.unwrap
        )
        scroller setTargetPosition position
        layoutManager startSmoothScroll scroller
      }
    } yield ()
  }
  def scrollTo(position: Int, timePerInch: Float = 45F): CallbackTask[Unit] = task of {
    (done: OnFinish) => for {
      ui <- task {
        UiThread via recyclerView
      }
      scroller <- task {
        new SmoothScroller(
          recyclerView.getContext, timePerInch, layoutManager,
          done.unwrap
        )
      }
      _ <- ui { _ =>
        scroller setTargetPosition position
        layoutManager startSmoothScroll scroller
      }
    } yield ()
  }

  def skipTo(position: Int): CallbackTask[Unit] = for {
    ui <- task {
      UiThread via recyclerView
    }
    _ <- ui { _ =>
      layoutManager.scrollToPositionWithOffset(position, 0)
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
  }
  override def onStop(): Unit = {
    onFinish(new ScrollerStopEvent)
  }
  override def calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float = {
    timePerInch / displayMetrics.densityDpi
  }
}
