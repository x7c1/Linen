package x7c1.linen.modern

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.{LinearLayoutManager, LinearSmoothScroller, RecyclerView}
import android.util.DisplayMetrics
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.decorator.UiThreadTask

import scala.collection.mutable

trait Pane {
  def displayPosition: Int
}

class EntryArea(
  val entries: EntryBuffer,
  sources: SourceAccessor,
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
    entries.firstEntryIdOf(sourceId) match {
      case Some(entryId) =>
        val position = entries indexOf entryId
        scrollTo(position){ _ => onComplete(new EntryDisplayedEvent) }
      case _ =>
        startLoading(sourceId)(onComplete)
    }
  }

  def startLoading(targetSourceId: Long)(onFinish: EntryDisplayedEvent => Unit) = {

    // deprecated
    val listener1 = new OnEntryLoadedListener {
      override def onEntryLoaded(e: EntryLoadedEvent): Unit = {
        sourceStateBuffer.updateState(e.sourceId, SourcePrefetched)
      }
    }
    val listener2 = OnEntryLoadedListener {
      case EntryLoadedEvent(sourceId, loadedEntries @ Seq(entry, _*)) =>
        val position = calculateEntryPositionOf(sourceId)
        val inserted = entries.insertAll(position, sourceId, loadedEntries)
        Log debug s"[done] entries(${inserted.length}) inserted"

        val task = taskToScroll(position, inserted.length)(onFinish)
        task()
    }
    val loader = new EntryLoader(entryCacher, listener1 append listener2)
    loader load targetSourceId
  }

  def taskToScroll(position: Int, insertedLength: Int)
    (onFinish: EntryDisplayedEvent => Unit) = {

    val ui = UiThreadTask from recyclerView
    for {
      _ <- ui { view =>
        val current = layoutManager.findFirstCompletelyVisibleItemPosition()
        val base = if(current == position) -1 else 0
        view.getAdapter.notifyItemRangeInserted(position + base, insertedLength)
      }
    } yield scrollTo(position){ _ =>
      onFinish(new EntryDisplayedEvent)
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
  private def calculateEntryPositionOf(sourceId: Long): Int = {
    val previousId = sources.collectLastFrom(sourceId){
      case source if entries.has(source.id) => entries.lastEntryIdOf(source.id)
    }
    entries positionAfter previousId.flatten
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
