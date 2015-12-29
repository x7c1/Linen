package x7c1.linen.modern.init

import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.res.layout.{MainLayout, SourceRow}
import x7c1.linen.modern.action.observer.{SourceFocusedObserver, SourceSelectedObserver, SourceSkipStoppedObserver, SourceSkippedObserver}
import x7c1.linen.modern.action.{Actions, SourceFocusedEventFactory, SourceSkipStoppedFactory, SourceSkippedEventFactory}
import x7c1.linen.modern.display.{PaneDragDetector, PaneLabel, SourceRowAdapter}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.observer.{FocusDetector, SkipDetector, SkipPositionFinder}

trait SourceAreaInitializer {
  def layout: MainLayout
  def accessors: Accessors
  def actions: Actions
  def sourceRowProvider: ViewHolderProvider[SourceRow]

  def displaySize: Point
  def dipToPixel(dip: Int): Int

  def setupSourceArea(): Unit = {
    layout.sourceArea setLayoutParams {
      val radius = 20
      val margin = 18
      val params = layout.sourceArea.getLayoutParams
      params.width = displaySize.x - dipToPixel(margin + radius)
      params
    }
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
    layout.sourceList addOnItemTouchListener PaneDragDetector.create(
      context = layout.sourceList.getContext,
      label = PaneLabel.SourceArea,
      actions = actions,
      onTouch = forFocus
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
