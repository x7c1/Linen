package x7c1.linen.modern.init

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.modern.action.observer.{EntryDetailFocusedObserver, EntryDetailSelectedObserver, EntryDetailSkipStoppedObserver, EntryDetailSkippedObserver}
import x7c1.linen.modern.action.{EntryDetailFocusedEventFactory, EntrySkipStoppedFactory, EntrySkippedEventFactory}
import x7c1.linen.modern.display.{EntryDetailRowAdapter, PaneDragDetector}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.observer.{FocusDetector, SkipDetector, SkipPositionFinder}

trait EntryDetailAreaInitializer {
  self: ContainerInitializer =>

  def setupEntryDetailArea(): Unit = {
    layout.entryDetailArea setLayoutParams {
      val params = layout.entryDetailArea.getLayoutParams
      params.width = displaySize.x
      params
    }
    layout.entryDetailToolbar onClickNavigation { _ =>
      actions.container.onBack()
    }
    val manager = new LinearLayoutManager(layout.entryDetailList.getContext)
    layout.entryDetailList setLayoutManager manager
    layout.entryDetailList setAdapter new EntryDetailRowAdapter(
      accessors.entryDetail,
      new EntryDetailSelectedObserver(actions),
      entryDetailRowProvider
    )
    val forFocus = FocusDetector.forLinearLayoutManager(
      recyclerView = layout.entryDetailList,
      focusedEventFactory = new EntryDetailFocusedEventFactory(accessors.entryDetail),
      onFocused = new EntryDetailFocusedObserver(actions)
    )
    layout.entryDetailList addOnItemTouchListener PaneDragDetector.create(
      context = layout.entryDetailList.getContext,
      from = container.entryDetailArea,
      actions = actions,
      onTouch = forFocus
    )
    val forSkip = SkipDetector.createListener(
      context = layout.detailToNext.getContext,
      positionFinder = SkipPositionFinder createBy manager,
      skippedEventFactory = new EntrySkippedEventFactory(accessors.entryOutline),
      skipDoneEventFactory = new EntrySkipStoppedFactory(accessors.entryOutline),
      onSkippedListener = new EntryDetailSkippedObserver(actions),
      onSkipDoneListener = new EntryDetailSkipStoppedObserver(actions)
    )
    layout.detailToNext setOnTouchListener forSkip
    layout.detailBottomBar setOnTouchListener forSkip
  }
}
