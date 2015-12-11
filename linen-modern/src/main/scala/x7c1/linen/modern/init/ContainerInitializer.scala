package x7c1.linen.modern.init

import java.lang.Math.max

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import x7c1.linen.glue.res.layout.{EntryDetailRow, EntryRow, MainLayout, SourceRow}
import x7c1.linen.modern.accessor.{EntryBuffer, SourceBuffer, SourceStateBuffer}
import x7c1.linen.modern.action.observer.{EntryDetailFocusedObserver, EntryDetailSelectedObserver, EntryFocusedObserver, EntrySelectedObserver, SourceFocusedObserver, SourceSelectedObserver, SourceSkippedDetector, SourceSkippedObserver}
import x7c1.linen.modern.action.{Actions, ContainerAction, EntryAreaAction, EntryDetailAreaAction, EntryDetailFocusedEventFactory, EntryFocusedEventFactory, SourceAreaAction, SourceFocusedEventFactory, SourceSkippedEventFactory}
import x7c1.linen.modern.display.{EntryArea, EntryDetailArea, EntryDetailRowAdapter, EntryRowAdapter, PaneContainer, SourceArea, SourceRowAdapter}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.observer.FocusDetector


class ContainerInitializer(
  activity: Activity,
  layout: MainLayout,
  sourceRowProvider: ViewHolderProvider[SourceRow],
  entryRowProvider: ViewHolderProvider[EntryRow],
  entryDetailRowProvider: ViewHolderProvider[EntryDetailRow]) {

  def setup(): Unit = {
    updateWidth(0.85, layout.sourceArea)
    updateWidth(0.9, layout.entryArea)
    updateWidth(0.95, layout.entryDetailArea)

    DummyFactory.setup(layout, activity)

    setupSourceArea()
    setupEntryArea()
    setupEntryDetailArea()
  }
  private def setupSourceArea() = {
    val manager = new LinearLayoutManager(activity)
    val adapter = new SourceRowAdapter(
      sourceBuffer,
      sourceStateBuffer,
      new SourceSelectedObserver(actions),
      sourceRowProvider
    )
    layout.sourceList setLayoutManager manager
    layout.sourceList setAdapter adapter
    layout.sourceList setOnTouchListener FocusDetector.createListener(
      recyclerView = layout.sourceList,
      getPosition = () => manager.findFirstCompletelyVisibleItemPosition(),
      focusedEventFactory = new SourceFocusedEventFactory(sourceBuffer),
      onFocused = new SourceFocusedObserver(actions)
    )
    layout.sourceToNext setOnTouchListener SourceSkippedDetector.createListener(
      context = layout.sourceToNext.getContext,
      skippedEventFactory = new SourceSkippedEventFactory(manager, sourceBuffer),
      onSkippedListener = new SourceSkippedObserver(actions)
    )
  }
  private def setupEntryArea() = {
    val manager = new LinearLayoutManager(activity)
    val adapter = new EntryRowAdapter(
      entryBuffer,
      new EntrySelectedObserver(actions),
      entryRowProvider
    )
    layout.entryList setLayoutManager manager
    layout.entryList setAdapter adapter
    layout.entryList setOnTouchListener FocusDetector.createListener(
      recyclerView = layout.entryList,
      getPosition = () => manager.findFirstCompletelyVisibleItemPosition(),
      focusedEventFactory = new EntryFocusedEventFactory(entryBuffer),
      onFocused = new EntryFocusedObserver(actions)
    )
  }
  private def setupEntryDetailArea() = {
    val manager = new LinearLayoutManager(activity)
    val adapter = new EntryDetailRowAdapter(
      entryFullContentBuffer,
      new EntryDetailSelectedObserver(actions),
      entryDetailRowProvider
    )
    val getPosition = () => {
      manager.findFirstCompletelyVisibleItemPosition() match {
        case n if n < 0 => manager.findFirstVisibleItemPosition()
        case n => n
      }
    }
    layout.entryDetailList setLayoutManager manager
    layout.entryDetailList setAdapter adapter
    layout.entryDetailList setOnTouchListener FocusDetector.createListener(
      recyclerView = layout.entryDetailList,
      getPosition = getPosition,
      focusedEventFactory = new EntryDetailFocusedEventFactory(entryFullContentBuffer),
      onFocused = new EntryDetailFocusedObserver(actions)
    )
  }

  private lazy val displaySize = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }
  private lazy val actions = {
    new Actions(
      new ContainerAction(container),
      new SourceAreaAction(container, sourceBuffer),
      new EntryAreaAction(
        container = container,
        sourceAccessor = sourceBuffer,
        entryAccessor = entryBuffer
      ),
      new EntryDetailAreaAction(container, entryFullContentBuffer)
    )
  }

  private lazy val sourceBuffer = {
    SourceBuffer.create(activity)
  }

  private lazy val sourceStateBuffer = new SourceStateBuffer

  private lazy val sourceArea = {
    new SourceArea(
      sources = sourceBuffer,
      recyclerView = layout.sourceList,
      getPosition = () => panePosition of layout.sourceArea
    )
  }
  private lazy val entryBuffer = {
    EntryBuffer.createOutline(activity)
  }

  private lazy val entryFullContentBuffer = {
    EntryBuffer.createFullContent(activity)
  }

  private lazy val entryArea = {
    new EntryArea(
      toolbar = layout.entryToolbar,
      recyclerView = layout.entryList,
      getPosition = () => panePosition of layout.entryArea
    )
  }

  private lazy val entryDetailArea =
    new EntryDetailArea(
      sources = sourceBuffer,
      entries = entryBuffer,
      toolbar = layout.entryDetailToolbar,
      recyclerView = layout.entryDetailList,
      getPosition = () => panePosition of layout.entryDetailArea
    )

  private lazy val panePosition = {
    val length = layout.paneContainer.getChildCount
    val children = 0 to (length - 1) map layout.paneContainer.getChildAt
    new PanePosition(children, displaySize.x)
  }
  private lazy val container =
    new PaneContainer(
      layout.paneContainer,
      sourceArea,
      entryArea,
      entryDetailArea
    )

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
