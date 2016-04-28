package x7c1.linen.modern.init.unread

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.modern.action.observer.{OutlineFocusedObserver, OutlineSelectedObserver, OutlineSkipStoppedObserver, OutlineSkippedObserver}
import x7c1.linen.modern.action.{EntrySkipStopped, EntrySkipStoppedFactory, EntrySkippedEventFactory, OutlineFocusedEvent, OutlineFocusedEventFactory}
import x7c1.linen.modern.display.unread.{OutlineRowAdapter, PaneDragDetector}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.observer.{FocusDetector, OnItemFocusedListener, OnSkipStoppedListener, SkipDetector, SkipPositionFinder}

trait OutlineAreaInitializer {
  self: UnreadItemsDelegatee =>

  def setupEntryArea(): Unit = {
    layout.entryArea updateLayoutParams { _.width = widthWithMargin }
    layout.entryToolbar onClickNavigation { _ =>
      actions.container.onBack()
    }
    val manager = new LinearLayoutManager(layout.entryList.getContext)
    layout.entryList setLayoutManager manager

    layout.entryList setAdapter new OutlineRowAdapter(
      accessors.entryOutline,
      new OutlineSelectedObserver(actions),
      unreadRowProviders.forOutlineArea,
      footerHeightOf(layout.entryList)
    )
    val forFocus = FocusDetector.forLinearLayoutManager(
      recyclerView = layout.entryList,
      focusedEventFactory = new OutlineFocusedEventFactory(accessors.entryOutline),
      onFocused = new OutlineFocusedObserver(actions) append outlineMarker

    )
    layout.entryList addOnItemTouchListener PaneDragDetector.create(
      context = layout.entryList.getContext,
      from = container.outlineArea,
      actions = actions,
      onTouch = forFocus
    )
    val forSkip = SkipDetector.createListener(
      context = layout.entryToNext.getContext,
      positionFinder = SkipPositionFinder createBy manager,
      skippedEventFactory = new EntrySkippedEventFactory(accessors.entryOutline),
      skipDoneEventFactory = new EntrySkipStoppedFactory(accessors.entryOutline),
      onSkippedListener = new OutlineSkippedObserver(actions),
      onSkipDoneListener = new OutlineSkipStoppedObserver(actions) append outlineMarker
    )
    layout.entryToNext setOnTouchListener forSkip
    layout.entryBottomBar setOnTouchListener forSkip
  }
  protected lazy val outlineMarker = new OutlineEntryMarker(helper, entryMarker)
}

class OutlineEntryMarker(helper: DatabaseHelper, marker: BrowsedEntriesMarker)
  extends OnItemFocusedListener[OutlineFocusedEvent]
  with OnSkipStoppedListener[EntrySkipStopped]{

  override def onFocused(event: OutlineFocusedEvent): Unit = {
    marker noteOutlinePosition event.position
  }
  override def onSkipStopped(event: EntrySkipStopped): Unit = {
    marker noteOutlinePosition event.currentPosition
  }
}
