package x7c1.linen.modern.init

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.res.layout.{EntryRow, MainLayout}
import x7c1.linen.modern.action.{EntrySkipStoppedFactory, EntrySkippedEventFactory, EntryFocusedEventFactory, Actions}
import x7c1.linen.modern.action.observer.{EntrySkipStoppedObserver, EntrySkippedObserver, EntryFocusedObserver, EntrySelectedObserver}
import x7c1.linen.modern.display.EntryRowAdapter
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.observer.{SkipPositionFinder, SkipDetector, FocusDetector}

trait EntryAreaInitializer {
  def layout: MainLayout
  def accessors: Accessors
  def actions: Actions
  def entryRowProvider: ViewHolderProvider[EntryRow]

  def setupEntryArea() = {
    val manager = new LinearLayoutManager(layout.entryList.getContext)
    layout.entryList setLayoutManager manager
    layout.entryList setAdapter new EntryRowAdapter(
      accessors.entryOutline,
      new EntrySelectedObserver(actions),
      entryRowProvider
    )
    layout.entryList setOnTouchListener FocusDetector.createListener(
      recyclerView = layout.entryList,
      getPosition = () => manager.findFirstCompletelyVisibleItemPosition(),
      focusedEventFactory = new EntryFocusedEventFactory(accessors.entryOutline),
      onFocused = new EntryFocusedObserver(actions)
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
