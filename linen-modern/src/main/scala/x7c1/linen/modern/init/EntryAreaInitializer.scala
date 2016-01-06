package x7c1.linen.modern.init

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.res.layout.{EntryRow, MainLayout}
import x7c1.linen.modern.action.observer.{EntryFocusedObserver, EntrySelectedObserver, EntrySkipStoppedObserver, EntrySkippedObserver}
import x7c1.linen.modern.action.{Actions, EntryFocusedEventFactory, EntrySkipStoppedFactory, EntrySkippedEventFactory}
import x7c1.linen.modern.display.{EntryRowAdapter, PaneDragDetector, PaneLabel}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.observer.{FocusDetector, SkipDetector, SkipPositionFinder}

trait EntryAreaInitializer {
  def layout: MainLayout
  def accessors: Accessors
  def actions: Actions
  def entryRowProvider: ViewHolderProvider[EntryRow]
  def widthWithMargin: Int

  def setupEntryArea(): Unit = {
    layout.entryArea setLayoutParams {
      val params = layout.entryArea.getLayoutParams
      params.width = widthWithMargin
      params
    }
    val manager = new LinearLayoutManager(layout.entryList.getContext)
    layout.entryList setLayoutManager manager
    layout.entryList setAdapter new EntryRowAdapter(
      accessors.entryOutline,
      new EntrySelectedObserver(actions),
      entryRowProvider
    )
    val forFocus = FocusDetector.forLinearLayoutManager(
      recyclerView = layout.entryList,
      focusedEventFactory = new EntryFocusedEventFactory(accessors.entryOutline),
      onFocused = new EntryFocusedObserver(actions)
    )
    layout.entryList addOnItemTouchListener PaneDragDetector.create(
      context = layout.entryList.getContext,
      label = PaneLabel.EntryArea,
      actions = actions,
      onTouch = forFocus
    )
    layout.entryToNext setOnTouchListener SkipDetector.createListener(
      context = layout.entryToNext.getContext,
      positionFinder = SkipPositionFinder createBy manager,
      skippedEventFactory = new EntrySkippedEventFactory(accessors.entryOutline),
      skipDoneEventFactory = new EntrySkipStoppedFactory(accessors.entryOutline),
      onSkippedListener = new EntrySkippedObserver(actions),
      onSkipDoneListener = new EntrySkipStoppedObserver(actions)
    )
  }
}
