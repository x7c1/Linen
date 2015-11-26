package x7c1.linen.modern

import java.lang.Math.max

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import x7c1.linen.glue.res.layout.{EntryRow, MainLayout, SourceRow}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._

class ContainerInitializer(
  activity: Activity,
  layout: MainLayout,
  sourceRowProvider: ViewHolderProvider[SourceRow],
  entryRowProvider: ViewHolderProvider[EntryRow]) {

  def setup(): Unit = {
    updateWidth(0.85, layout.swipeLayoutLeft)
    updateWidth(0.9, layout.swipeLayoutCenter)
    updateWidth(0.95, layout.swipeLayoutRight)

    setupSourceArea()
    setupEntryArea()
  }
  private def setupSourceArea() = {
    val manager = new LinearLayoutManager(activity)
    val prefetcher = new EntryPrefetcher(
      sourceBuffer,
      onSourceEntryLoaded,
      entryCacher
    )
    val adapter = new SourceRowAdapter(
      sourceBuffer,
      sourceStateBuffer,
      new SourceSelectObserver(container, prefetcher),
      sourceRowProvider
    )
    lazy val observer = new SourceFocusObserver(
      sourceBuffer,
      prefetcher,
      entryArea
    )
    layout.sampleLeftList setLayoutManager manager
    layout.sampleLeftList setAdapter adapter
    layout.sampleLeftList onTouch ItemFocusDetector.createOnTouch(
      recyclerView = layout.sampleLeftList,
      layoutManager = manager,
      onItemFocused = observer
    )
  }
  private def setupEntryArea() = {
    val manager = new LinearLayoutManager(activity)
    val adapter = new EntryRowAdapter(
      entryBuffer,
      new EntrySelectObserver(container),
      entryRowProvider
    )
    lazy val observer = new EntryFocusObserver(
      entryBuffer,
      entryArea,
      sourceArea
    )
    layout.sampleCenterList setLayoutManager manager
    layout.sampleCenterList setAdapter adapter
    layout.sampleCenterList onTouch ItemFocusDetector.createOnTouch(
      recyclerView = layout.sampleCenterList,
      layoutManager = manager,
      onItemFocused = observer
    )
  }

  private lazy val displaySize = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }

  private lazy val sourceBuffer = new SourceBuffer

  private lazy val sourceStateBuffer = new SourceStateBuffer

  private lazy val entryCacher = new EntryCacher

  private lazy val sourceArea = {
    new SourceArea(
      sources = sourceBuffer,
      recyclerView = layout.sampleLeftList,
      getPosition = () => panePosition of layout.swipeLayoutLeft
    )
  }

  private lazy val onSourceEntryLoaded =
    new SourceStateUpdater(sourceStateBuffer) append
    new SourceChangedNotifier(sourceBuffer, layout.sampleLeftList)

  private lazy val entryBuffer = new EntryBuffer

  private lazy val entryArea = {
    new EntryArea(
      entries = entryBuffer,
      sources = sourceBuffer,
      onEntryLoaded = onSourceEntryLoaded,
      toolbar = layout.entryToolbar,
      actions = new EntryAreaActions(layout.sampleCenterList),
      entryCacher = entryCacher,
      getPosition = () => panePosition of layout.swipeLayoutCenter
    )
  }
  private lazy val panePosition = {
    val length = layout.swipeContainer.getChildCount
    val children = 0 to (length - 1) map layout.swipeContainer.getChildAt
    new PanePosition(children, displaySize.x)
  }
  private lazy val container = {
    new PaneContainer(
      layout.swipeContainer,
      sourceArea,
      entryArea
    )
  }
  private def updateWidth(ratio: Double, view: View): Unit = {
    val params = view.getLayoutParams
    params.width = (ratio * displaySize.x).toInt
    view setLayoutParams params
  }
}

private class PanePosition(children: Seq[View], displayWidth: Int){
  def of(view: View): Int = {
    positions find (_._1 == view) map (_._2) getOrElse {
      throw new IllegalStateException("view not found")
    }
  }
  private lazy val positions = {
    val xs = children.scanLeft(0){_ + _.getWidth}
    children.zip(xs).zipWithIndex map { case ((view, start), i) =>
      val position =
        if (i == children.length - 1)
          start - (displayWidth - view.getWidth)
        else
          start - (displayWidth - view.getWidth) / 2

      view -> max(0, position)
    }
  }
}
