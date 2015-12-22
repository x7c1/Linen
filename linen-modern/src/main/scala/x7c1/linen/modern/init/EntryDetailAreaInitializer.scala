package x7c1.linen.modern.init

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.res.layout.{EntryDetailRow, MainLayout}
import x7c1.linen.modern.action.{Actions, EntrySkipStoppedFactory, EntrySkippedEventFactory, EntryDetailFocusedEventFactory}
import x7c1.linen.modern.action.observer.{EntryDetailSkipStoppedObserver, EntryDetailSkippedObserver, EntryDetailFocusedObserver, EntryDetailSelectedObserver}
import x7c1.linen.modern.display.EntryDetailRowAdapter
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.observer.{SkipPositionFinder, SkipDetector, FocusDetector}

trait EntryDetailAreaInitializer {
  def layout: MainLayout
  def accessors: Accessors
  def actions: Actions
  def entryDetailRowProvider: ViewHolderProvider[EntryDetailRow]

  def setupEntryDetailArea(): Unit = {
    val manager = new LinearLayoutManager(layout.entryDetailList.getContext)
    layout.entryDetailList setLayoutManager manager
    layout.entryDetailList setAdapter new EntryDetailRowAdapter(
      accessors.entryDetail,
      new EntryDetailSelectedObserver(actions),
      entryDetailRowProvider
    )
    layout.entryDetailList setOnTouchListener FocusDetector.forLinearLayoutManager(
      recyclerView = layout.entryDetailList,
      focusedEventFactory = new EntryDetailFocusedEventFactory(accessors.entryDetail),
      onFocused = new EntryDetailFocusedObserver(actions)
    )
    layout.detailToNext setOnTouchListener SkipDetector.createListener(
      context = layout.detailToNext.getContext,
      positionFinder = SkipPositionFinder createBy manager,
      skippedEventFactory = new EntrySkippedEventFactory(accessors.entryOutline),
      skipDoneEventFactory = new EntrySkipStoppedFactory(accessors.entryOutline),
      onSkippedListener = new EntryDetailSkippedObserver(actions),
      onSkipDoneListener = new EntryDetailSkipStoppedObserver(actions)
    )
  }
}
