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

  private var loading = false

  def displayOrLoad(sourceId: Long)(onFinish: EntryDisplayedEvent => Unit): Unit = {
    Log info s"[init] sourceId:$sourceId"

    if (loading){
      Log info s"[abort] already loading"
      return
    }
    loading = true

    val onComplete = (e: EntryDisplayedEvent) => {
      Log info s"[done] sourceId:$sourceId"
      loading = false
      onFinish(e)
    }
    sources.firstEntryIdOf(sourceId) match {
      case Some(entryId) =>
        val position = entries indexOf entryId
        scrollTo(position){ _ => onComplete(new EntryDisplayedEvent) }
      case _ =>
        startLoading(sourceId)(onComplete)
    }
  }
  def startLoading(sourceId: Long)(onFinish: EntryDisplayedEvent => Unit) = {
    EntryLoader.load(sourceId){ case e: EntriesLoadSuccess =>
      val newer = e.entries filterNot { sources has _.sourceId }
      val position = entries positionAfter sources.entryIdBefore(sourceId)

      entries.insertAll(position, newer)
      sources.updateMapping(sourceId, e.entries.map(_.entryId))

      Log info s"[done] entries(${newer.length}) inserted"

      recyclerView runUi { view =>
        view.getAdapter.notifyDataSetChanged()
        e.entries.headOption.foreach { entry =>
          val y = entries indexOf entry.entryId
          scrollTo(y) { _ => onFinish(new EntryDisplayedEvent) }
        }
      }
    }
  }

  def scrollTo(position: Int)(onFinish: ScrollerStopEvent => Unit): Unit = {
    Log info s"[init] position:$position"

    val scroller = new SmoothScroller(
      recyclerView.getContext, speed = 25F, layoutManager, onFinish
    )
    scroller setTargetPosition position
    layoutManager startSmoothScroll scroller
  }
  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
}

class EntryDisplayedEvent

class SourceArea(
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  def scrollTo(position: Int)(onFinish: ScrollerStopEvent => Unit): Unit = {
    Log info s"[init] position:$position"

    val scroller = new SmoothScroller(
      recyclerView.getContext, speed = 75F, layoutManager, onFinish
    )
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

class ScrollerStopEvent

class SmoothScroller(
  context: Context,
  speed: Float,
  layoutManager: LinearLayoutManager,
  onFinish: ScrollerStopEvent => Unit) extends LinearSmoothScroller(context: Context) {

  override def computeScrollVectorForPosition(i: Int): PointF = {
    layoutManager.computeScrollVectorForPosition(i)
  }
  override def getVerticalSnapPreference: Int = {
    LinearSmoothScroller.SNAP_TO_START
  }
  override def onStart() = {
    Log debug s"[init]"
  }
  override def onStop(): Unit = {
    Log debug s"[done]"
    onFinish(new ScrollerStopEvent)
  }
  override def calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float = {
    speed / displayMetrics.densityDpi
  }
}
