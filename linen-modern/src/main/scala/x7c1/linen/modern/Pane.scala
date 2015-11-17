package x7c1.linen.modern

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.{LinearLayoutManager, LinearSmoothScroller, RecyclerView}
import android.util.DisplayMetrics
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.chrono.BufferingTimer

trait Pane {
  def displayPosition: Int
}

class EntriesArea(
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  def displayOrLoad(sourceId: Long)(onFinish: EntriesLoadedEvent => Unit): Unit = {
    Log info s"[init] sourceId:$sourceId"

    Thread sleep 500
    onFinish(new EntriesLoadedEvent)
  }
}

class EntriesLoadedEvent

class SourcesArea(
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  import x7c1.wheat.modern.decorator.Imports._
  private val timer = new BufferingTimer(delay = 100)

  recyclerView onScroll { e =>
    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
    timer touch {
      onSourceFocused onSourceFocused new SourceFocusedEvent(position)
    }
  }
  override lazy val displayPosition: Int = getPosition()

  def onSourceFocused = new OnSourceFocusedListener {
    override def onSourceFocused(event: SourceFocusedEvent): Unit = {
      Log info event.position.toString
    }
  }
  lazy val layoutManager: LinearLayoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
  def scrollTo(position: Int)(onFinish: SourceScrolledEvent => Unit): Unit = {
    Log info s"[init] position:$position"

    val scroller = new SmoothScroller(recyclerView.getContext, layoutManager, onFinish)
    scroller setTargetPosition position
    layoutManager startSmoothScroll scroller
  }
}

case class SourceFocusedEvent(position: Int)
trait OnSourceFocusedListener {
  def onSourceFocused(event:  SourceFocusedEvent)
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
