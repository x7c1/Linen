package x7c1.linen.modern.init

import java.lang.Math.abs

import android.support.v7.widget.LinearLayoutManager
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import x7c1.linen.glue.res.layout.{MainLayout, SourceRow}
import x7c1.linen.modern.action.observer.{SourceFocusedObserver, SourceSelectedObserver, SourceSkipStoppedObserver, SourceSkippedObserver}
import x7c1.linen.modern.action.{Actions, SourceFocusedEventFactory, SourceSkipStoppedFactory, SourceSkippedEventFactory}
import x7c1.linen.modern.display.{PaneDragStoppedEventFactory, PaneLabel, SourceRowAdapter}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.observer.recycler.DragDetector
import x7c1.wheat.modern.observer.{FocusDetector, SkipDetector, SkipPositionFinder}

trait SourceAreaInitializer {
  def layout: MainLayout
  def accessors: Accessors
  def actions: Actions
  def sourceRowProvider: ViewHolderProvider[SourceRow]

  def setupSourceArea(): Unit = {
    val manager = new LinearLayoutManager(layout.sourceList.getContext)
    layout.sourceList setLayoutManager manager
    layout.sourceList setAdapter new SourceRowAdapter(
      accessors.source,
      new SourceSelectedObserver(actions),
      sourceRowProvider
    )
    val forFocus = FocusDetector.forLinearLayoutManager(
      recyclerView = layout.sourceList,
      focusedEventFactory = new SourceFocusedEventFactory(accessors.source),
      onFocused = new SourceFocusedObserver(actions)
    )
    layout.sourceList addOnItemTouchListener new DragDetector(
      context = layout.sourceList.getContext,
      stoppedEventFactory = new PaneDragStoppedEventFactory(PaneLabel.SourceArea),
      onTouch = forFocus,
      onDrag = actions.container.onPaneDragging,
      onDragStopped = actions.container.onPaneDragStopped
    )
    layout.sourceToNext setOnTouchListener SkipDetector.createListener(
      context = layout.sourceToNext.getContext,
      positionFinder = SkipPositionFinder createBy manager,
      skippedEventFactory = new SourceSkippedEventFactory(accessors.source),
      skipDoneEventFactory = new SourceSkipStoppedFactory(accessors.source),
      onSkippedListener = new SourceSkippedObserver(actions),
      onSkipDoneListener = new SourceSkipStoppedObserver(actions)
    )
  }
}

private class PaneScrollFilter extends SimpleOnGestureListener {

  private var horizontalCount = 0

  override def onScroll(
    e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {

    val isHorizontal = abs(distanceX) > abs(distanceY)
    if (isHorizontal){
      horizontalCount += 1
    }
    val accepted = horizontalCount > 2
    accepted
  }
  override def onDown(e: MotionEvent): Boolean = {
    horizontalCount = 0
    false
  }
}
