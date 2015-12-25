package x7c1.linen.modern.init

import java.lang.Math.abs

import android.support.v7.widget.RecyclerView.SimpleOnItemTouchListener
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.GestureDetector.OnGestureListener
import android.view.{GestureDetector, MotionEvent}
import x7c1.linen.glue.res.layout.{MainLayout, SourceRow}
import x7c1.linen.modern.action.observer.{SourceFocusedObserver, SourceSelectedObserver, SourceSkipStoppedObserver, SourceSkippedObserver}
import x7c1.linen.modern.action.{Actions, SourceFocusedEventFactory, SourceSkipStoppedFactory, SourceSkippedEventFactory}
import x7c1.linen.modern.display.{PaneDragStoppedEvent, PaneFlingDetector, PaneLabel, SourceRowAdapter}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
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
    val forDragging = PaneFlingDetector.createListener(
      context = layout.sourceList.getContext,
      from = PaneLabel.SourceArea,
      onFlung = actions.container.onPaneDragging
    )
    layout.sourceList.addOnItemTouchListener(new SimpleOnItemTouchListener{
      val detector = new GestureDetector(
        layout.sourceList.getContext,
        new PaneScrollFilter
      )
      var previous: Option[Float] = None
      var direction: Option[Int] = None

      override def onTouchEvent(rv: RecyclerView, e: MotionEvent): Unit = {
        Log info s"${e.getRawX}, $previous"

        e.getAction match {
          case MotionEvent.ACTION_UP =>
            direction map { dir =>
              PaneDragStoppedEvent(PaneLabel.SourceArea, dir, e)
            } foreach {
              actions.container.onPaneDragStopped
            }
          case _ =>
            forDragging.onTouch(rv, e)
        }
        if (previous.contains(e.getRawX)){
          Log info s"keep"
        } else {
          Log info s"updated"
          direction = previous map (e.getRawX - _) map { x =>
            if (x > 0) 1
            else -1
          }
          previous = Some(e.getRawX)
        }

      }
      override def onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = {
        Log info s"$e"

        val isHorizontal = detector.onTouchEvent(e)
        if (isHorizontal){
          forDragging.onTouch(rv, e)
        } else {
          forFocus.onTouch(rv, e)
          forDragging.updateCurrentPosition(e)
        }
        isHorizontal
      }
    })

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

private class PaneScrollFilter extends OnGestureListener {
  override def onSingleTapUp(e: MotionEvent): Boolean = {
    false
  }
  override def onFling(
    e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = {

    false
  }
  override def onShowPress(e: MotionEvent): Unit = {}

  override def onLongPress(e: MotionEvent): Unit = {}

  var horizontalCount = 0

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
    Log info s"${e.getX}"

    horizontalCount = 0
    false
  }
}