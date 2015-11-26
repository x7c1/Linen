package x7c1.linen.modern

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.{Toolbar, LinearLayoutManager, LinearSmoothScroller, RecyclerView}
import android.util.DisplayMetrics
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.OnFinish
import x7c1.wheat.modern.decorator.Imports._

import scala.collection.mutable

trait Pane {
  def displayPosition: Int
}

class EntryArea(
  entries: EntryBuffer,
  sources: SourceAccessor,
  onEntryLoaded: OnEntryLoadedListener,
  toolbar: Toolbar,
  actions: EntryAreaActions,
  entryCacher: EntryCacher,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val loadingMap = mutable.Map[Long, Boolean]()

  def isLoading(sourceId: Long) = loadingMap.getOrElse(sourceId, false)

  def displayOrLoad(sourceId: Long)(done: OnFinish): Unit = {
    Log info s"[init] sourceId:$sourceId"

    if (isLoading(sourceId)){
      Log warn s"[cancel] (sourceId:$sourceId) already loading"
      return
    }
    loadingMap(sourceId) = true

    def execute(f: => Unit) = entries firstEntryIdOf sourceId match {
      case Some(entryId) =>
        val position = entries indexOf entryId
        actions.fastScrollTo(position)(OnFinish(f)).execute()
      case _ =>
        val onLoad = createListener(OnFinish(f))
        new EntryLoader(entryCacher, onLoad) load sourceId
    }
    execute {
      loadingMap(sourceId) = false
      updateToolbar(sourceId)

      Log info s"[done] sourceId:$sourceId"
      done.evalulate()
    }
  }

  def updateToolbar(sourceId: Long) = {
    val position = sources positionOf sourceId
    val source = sources get position
    toolbar runUi { _ setTitle source.title }
  }

  def scrollTo(position: Int)(done: OnFinish): Unit = {
    actions.scrollTo(position)(done).execute()
  }

  private def createListener(done: OnFinish) = {
    onEntryLoaded append OnEntryLoadedListener {
      case EntryLoadedEvent(sourceId, loadedEntries) =>
        val position = calculateEntryPositionOf(sourceId)
        val inserted = entries.insertAll(position, sourceId, loadedEntries)
        actions.afterInserting(position, inserted.length)(done).execute()
    }
  }

  private def calculateEntryPositionOf(sourceId: Long): Int = {
    val previousId = sources.collectLastFrom(sourceId){
      case source if entries.has(source.id) =>
        entries.lastEntryIdOf(source.id)
    }
    entries positionAfter previousId.flatten
  }

}

class SourceArea(
  sources: SourceAccessor,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  def display(sourceId: Long): OnFinish => Unit = done => {
    val position = sources.positionOf(sourceId)
    scrollTo(position)(done)
  }
  def scrollTo(position: Int): OnFinish => Unit = done => {
    Log info s"[init] position:$position"

    val scroller = new SmoothScroller(
      recyclerView.getContext, timePerInch = 75F, layoutManager,
      done.by[ScrollerStopEvent]
    )
    scroller setTargetPosition position
    layoutManager startSmoothScroll scroller
  }
  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
}

case class ItemFocusedEvent(position: Int){
  def dump: String = s"position:$position"
}

trait OnItemFocusedListener {
  def onItemFocused(event:  ItemFocusedEvent)
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
