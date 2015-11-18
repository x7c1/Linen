package x7c1.linen.modern

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.{LinearLayoutManager, LinearSmoothScroller, RecyclerView}
import android.util.DisplayMetrics
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

trait Pane {
  def displayPosition: Int
}

class EntryArea(
  val entries: EntryBuffer,
  sources: SourceBuffer,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  def displayOrLoad(sourceId: Long)(onFinish: EntryLoadedEvent => Unit): Unit = {
    Log info s"[init] sourceId:$sourceId"

    EntryLoader.load(sourceId){ case e: EntriesLoadSuccess =>
      val newer = e.entries filterNot { sources has _.sourceId }
      sources.entryIdBefore(sourceId) match {
        case Some(id) => entries.insertAfter(id, newer)
        case _ => entries appendAll newer
      }
      sources.updateMapping(sourceId, e.entries.map(_.entryId))
      Log info s"[done] append entries(${newer.length})"

      recyclerView runUi { _.getAdapter.notifyDataSetChanged() }
      onFinish(new EntryLoadedEvent)
    }
  }
}

class EntryLoadedEvent

class SourceArea(
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  def scrollTo(position: Int)(onFinish: SourceScrolledEvent => Unit): Unit = {
    Log info s"[init] position:$position"

    val scroller = new SmoothScroller(recyclerView.getContext, layoutManager, onFinish)
    scroller setTargetPosition position
    layoutManager startSmoothScroll scroller
  }
  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
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
