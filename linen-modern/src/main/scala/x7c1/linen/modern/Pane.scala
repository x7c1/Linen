package x7c1.linen.modern

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.{LinearLayoutManager, LinearSmoothScroller, RecyclerView}
import android.util.DisplayMetrics
import x7c1.wheat.macros.logger.Log

trait Pane {
  def displayPosition: Int
}

class EntriesArea(
  override val displayPosition: Int) extends Pane {

  def displayOrLoad(sourceId: Long)(onFinish: EntriesLoadedEvent => Unit): Unit = {
    Log info s"[init] sourceId:$sourceId"

    Thread sleep 500
    onFinish(new EntriesLoadedEvent)
  }
}

class EntriesLoadedEvent

class SourcesArea(
  recyclerView: RecyclerView,
  override val displayPosition: Int = 0) extends Pane {

  def scrollTo(position: Int)(onFinish: SourceScrolledEvent => Unit): Unit = {
    Log info s"[init] position:$position"

    val manager = recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
    val scroller = new SmoothScroller(recyclerView.getContext, manager, onFinish)
    scroller.setTargetPosition(position)
    manager.startSmoothScroll(scroller)
  }
}

class SourceScrolledEvent

class SmoothScroller(
  context: Context,
  manager: LinearLayoutManager,
  onFinish: SourceScrolledEvent => Unit) extends LinearSmoothScroller(context: Context) {

  override def computeScrollVectorForPosition(i: Int): PointF = {
    manager.computeScrollVectorForPosition(i)
  }
  override def getVerticalSnapPreference: Int = {
    LinearSmoothScroller.SNAP_TO_START
  }
  override def onStart() = {
    Log info s"[init]"
  }
  override def onStop(): Unit = {
    Log info s"[done]"
    onFinish(new SourceScrolledEvent)
  }
  override def calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float = {
    75.0F / displayMetrics.densityDpi
  }
}
