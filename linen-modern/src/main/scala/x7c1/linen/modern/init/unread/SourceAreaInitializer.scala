package x7c1.linen.modern.init.unread

import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity.START
import x7c1.linen.modern.action.observer.{SourceFocusedObserver, SourceSelectedObserver, SourceSkipStoppedObserver, SourceSkippedObserver}
import x7c1.linen.modern.action.{SourceFocusedEventFactory, SourceSkipStoppedFactory, SourceSkippedEventFactory}
import x7c1.linen.modern.display.unread.{PaneDragDetector, SourceRowAdapter}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.observer.{FocusDetector, SkipDetector, SkipPositionFinder}

trait SourceAreaInitializer {
  self: UnreadItemsDelegatee =>

  def setupSourceArea(): Unit = {
    layout.sourceArea setLayoutParams {
      val params = layout.sourceArea.getLayoutParams
      params.width = widthWithMargin
      params
    }
    layout.sourceToolbar setTitle "Technology"
    layout.sourceToolbar onClickNavigation { _ =>
      layout.drawerMenu openDrawer START
    }
    val manager = new LinearLayoutManager(layout.sourceList.getContext)
    layout.sourceList setLayoutManager manager
    layout.sourceList setAdapter new SourceRowAdapter(
      accessors.source,
      new SourceSelectedObserver(actions),
      unreadRowProviders.forSource
    )
    val forFocus = FocusDetector.forLinearLayoutManager(
      recyclerView = layout.sourceList,
      focusedEventFactory = new SourceFocusedEventFactory(accessors.source),
      onFocused = new SourceFocusedObserver(actions)
    )
    layout.sourceList addOnItemTouchListener PaneDragDetector.create(
      context = layout.sourceList.getContext,
      from = container.sourceArea,
      actions = actions,
      onTouch = forFocus
    )
    val forSkip = SkipDetector.createListener(
      context = layout.sourceToNext.getContext,
      positionFinder = SkipPositionFinder createBy manager,
      skippedEventFactory = new SourceSkippedEventFactory(accessors.source),
      skipDoneEventFactory = new SourceSkipStoppedFactory(accessors.source),
      onSkippedListener = new SourceSkippedObserver(actions),
      onSkipDoneListener = new SourceSkipStoppedObserver(actions)
    )
    layout.sourceToNext setOnTouchListener forSkip
    layout.sourceBottomBar setOnTouchListener forSkip
  }
}
