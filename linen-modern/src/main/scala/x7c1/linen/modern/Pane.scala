package x7c1.linen.modern

import java.util.{Timer, TimerTask}

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

  import x7c1.wheat.modern.decorator.Imports._
  private val timer = new BufferedTimer(delay = 100)

  recyclerView onScroll { e =>
    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
    timer touch {
      Log info position.toString
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

class BufferedTimer (delay: Int){
  private val timer = new Timer()
  private var task: Option[TimerTask] = None

  def touch[A](f: => A) = {
    task foreach { _.cancel() }
    task = Some apply new BufferTask(f)
    task foreach { timer.schedule(_, delay) }
  }
  private class BufferTask[A](f: => A) extends java.util.TimerTask {
    override def run(): Unit = f
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
