package x7c1.linen.modern

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.{LinearLayoutManager, LinearSmoothScroller, RecyclerView}
import android.util.DisplayMetrics
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

import scala.collection.mutable

trait Pane {
  def displayPosition: Int
}

class EntryArea(
  val entries: EntryBuffer,
  sources: SourceBuffer,
  sourceStateBuffer: SourceStateBuffer,
  entryCacher: EntryCacher,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val loadingMap = mutable.Map[Long, Boolean]()

  def isLoading(sourceId: Long) = loadingMap.getOrElse(sourceId, false)

  def displayOrLoad(sourceId: Long)(onFinish: EntryDisplayedEvent => Unit): Unit = {
    Log info s"[init] sourceId:$sourceId"

    if (isLoading(sourceId)){
      Log warn s"[abort] (sourceId:$sourceId) already loading"
      return
    }
    loadingMap(sourceId) = true

    val onComplete = (e: EntryDisplayedEvent) => {
      Log info s"[done] sourceId:$sourceId"
      loadingMap(sourceId) = false
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
    EntryLoader(entryCacher).load(sourceId){ e =>
      val newer = e.entries filterNot { sources has _.sourceId }
      val position = entries positionAfter sources.entryIdBefore(sourceId)
      val current = layoutManager.findFirstCompletelyVisibleItemPosition()

      entries.insertAll(position, newer)
      sources.updateMapping(sourceId, e.entries.map(_.entryId))
      sourceStateBuffer.updateState(sourceId, SourcePrefetched)

      Log debug s"[done] entries(${newer.length}) inserted"

      recyclerView runUi { view =>
        val base = if(current == position) -1 else 0
        view.getAdapter.notifyItemRangeInserted(position + base, newer.length)

        recyclerView runUi { _ =>
          e.entries.headOption.foreach { entry =>
            val y = entries indexOf entry.entryId
            scrollTo(y) { _ => onFinish(new EntryDisplayedEvent) }
          }
        }
      }
    }
  }

  def scrollTo(position: Int)(onFinish: ScrollerStopEvent => Unit): Unit = {
    Log info s"[init] position:$position"

    val scroller = new SmoothScroller(
      recyclerView.getContext, timePerInch = 125F, layoutManager, onFinish
    )
    val current = layoutManager.findFirstCompletelyVisibleItemPosition()
    val diff = current - position
    val space =
      if (diff < 0) -1
      else if(diff > 0) 1
      else 0

    recyclerView runUi { _ =>
      layoutManager.scrollToPositionWithOffset(position + space, 0)
      recyclerView runUi { _ =>
        scroller setTargetPosition position
        layoutManager startSmoothScroll scroller
      }
    }
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
      recyclerView.getContext, timePerInch = 75F, layoutManager, onFinish
    )
    scroller setTargetPosition position
    layoutManager startSmoothScroll scroller
  }
  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
}

case class SourceFocusedEvent(position: Int){
  def dump: String = s"position:$position"
}

trait OnSourceFocusedListener {
  def onSourceFocused(event:  SourceFocusedEvent)
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
    Log debug s"[init]"
  }
  override def onStop(): Unit = {
    Log debug s"[done]"
    onFinish(new ScrollerStopEvent)
  }
  override def calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float = {
    timePerInch / displayMetrics.densityDpi
  }
}
