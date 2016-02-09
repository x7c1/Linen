package x7c1.linen.modern.init.unread

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.modern.action.observer.{DetailSelectedObserver, DetailSkippedObserver, EntryDetailFocusedObserver, EntryDetailSkipStoppedObserver}
import x7c1.linen.modern.action.{DetailFocusedEventFactory, EntrySkipStoppedFactory, EntrySkippedEventFactory}
import x7c1.linen.modern.display.unread.{DetailRowAdapter, OnEntryVisitListener, PaneDragDetector}
import x7c1.linen.modern.struct.UnreadEntry
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.action.SiteVisitor
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.observer.{FocusDetector, SkipDetector, SkipPositionFinder}

trait DetailAreaInitializer {
  self: UnreadItemsDelegatee =>

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
    layout.entryDetailList setAdapter new DetailRowAdapter(
      accessors.entryDetail,
      new DetailSelectedObserver(actions),
      new OnEntryVisit(activity),
      unreadRowProviders.forDetail
    )
    val forFocus = FocusDetector.forLinearLayoutManager(
      recyclerView = layout.entryDetailList,
      focusedEventFactory = new DetailFocusedEventFactory(accessors.entryDetail),
      onFocused = new EntryDetailFocusedObserver(actions)
    )
    layout.entryDetailList addOnItemTouchListener PaneDragDetector.create(
      context = layout.entryDetailList.getContext,
      from = container.detailArea,
      actions = actions,
      onTouch = forFocus
    )
    val forSkip = SkipDetector.createListener(
      context = layout.detailToNext.getContext,
      positionFinder = SkipPositionFinder createBy manager,
      skippedEventFactory = new EntrySkippedEventFactory(accessors.entryOutline),
      skipDoneEventFactory = new EntrySkipStoppedFactory(accessors.entryOutline),
      onSkippedListener = new DetailSkippedObserver(actions),
      onSkipDoneListener = new EntryDetailSkipStoppedObserver(actions)
    )
    layout.detailToNext setOnTouchListener forSkip
    layout.detailBottomBar setOnTouchListener forSkip
  }
}

class OnEntryVisit[A <: UnreadEntry](context: Context) extends OnEntryVisitListener[A]{
  override def onVisit(target: A): Unit = {
    Log info target.url
    SiteVisitor(context) open target
  }
}
