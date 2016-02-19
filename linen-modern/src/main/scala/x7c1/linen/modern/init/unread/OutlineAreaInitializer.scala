package x7c1.linen.modern.init.unread

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.modern.action.observer.{OutlineFocusedObserver, OutlineSelectedObserver, OutlineSkipStoppedObserver, OutlineSkippedObserver}
import x7c1.linen.modern.action.{EntrySkipStoppedFactory, EntrySkippedEventFactory, OutlineFocusedEventFactory}
import x7c1.linen.modern.display.unread.{OutlineRowAdapter, PaneDragDetector}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.observer.recycler.{Next, Previous, VerticalDragDetector}
import x7c1.wheat.modern.observer.{FocusDetector, SkipDetector, SkipPositionFinder}

trait OutlineAreaInitializer {
  self: UnreadItemsDelegatee =>

  def setupEntryArea(): Unit = {
    layout.entryArea setLayoutParams {
      val params = layout.entryArea.getLayoutParams
      params.width = widthWithMargin
      params
    }
    layout.entryToolbar onClickNavigation { _ =>
      actions.container.onBack()
    }
    val manager = new LinearLayoutManager(layout.entryList.getContext)
    layout.entryList setLayoutManager manager
    layout.entryList setAdapter new OutlineRowAdapter(
      accessors.entryOutline,
      new OutlineSelectedObserver(actions),
      unreadRowProviders.forOutlineSource,
      unreadRowProviders.forOutlineEntry
    )
    val forFocus = FocusDetector.forLinearLayoutManager(
      recyclerView = layout.entryList,
      focusedEventFactory = new OutlineFocusedEventFactory(accessors.entryOutline),
      onFocused = new OutlineFocusedObserver(actions)
    )
    layout.entryList addOnItemTouchListener PaneDragDetector.create(
      context = layout.entryList.getContext,
      from = container.outlineArea,
      actions = actions,
      onTouch = forFocus
    )

    var targetPosition: Option[Int] = None

    layout.entryList addOnItemTouchListener VerticalDragDetector.create(
      context = activity,
      flingDistanceThreshold = dipToPixel(125),
      flingVelocityThreshold = dipToPixel(400),
      onFling = e => {
        val position = e.direction match {
          case Next =>
            targetPosition
          case Previous =>
            val pos = Math.max(0, manager.findFirstVisibleItemPosition() - 10)
            Some(pos)
        }
        position foreach { y =>
          container.outlineArea.scrollTo(y, timePerInch = 50).execute()
        }
      },
      onDragStart = () => {
        targetPosition = Some(manager.findLastVisibleItemPosition())
      },
      onDrag = e => {
        layout.entryList.scrollBy(0, - e.distance.toInt)
      },
      onDragStopped = e => {
        val position = e.direction match {
          case Next =>
            manager.findFirstCompletelyVisibleItemPosition()
          case Previous =>
            manager.findFirstVisibleItemPosition()
        }
        container.outlineArea.scrollTo(position, timePerInch = 50).execute()
      }
    )
    val forSkip = SkipDetector.createListener(
      context = layout.entryToNext.getContext,
      positionFinder = SkipPositionFinder createBy manager,
      skippedEventFactory = new EntrySkippedEventFactory(accessors.entryOutline),
      skipDoneEventFactory = new EntrySkipStoppedFactory(accessors.entryOutline),
      onSkippedListener = new OutlineSkippedObserver(actions),
      onSkipDoneListener = new OutlineSkipStoppedObserver(actions)
    )
    layout.entryToNext setOnTouchListener forSkip
    layout.entryBottomBar setOnTouchListener forSkip
  }
}
