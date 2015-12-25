package x7c1.linen.modern.init

import java.lang.Math.abs

import android.support.v7.widget.RecyclerView.SimpleOnItemTouchListener
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.{GestureDetector, MotionEvent}
import x7c1.linen.glue.res.layout.{MainLayout, SourceRow}
import x7c1.linen.modern.action.observer.{SourceFocusedObserver, SourceSelectedObserver, SourceSkipStoppedObserver, SourceSkippedObserver}
import x7c1.linen.modern.action.{Actions, SourceFocusedEventFactory, SourceSkipStoppedFactory, SourceSkippedEventFactory}
import x7c1.linen.modern.display.{OnTouchToScrollPane, PaneDragStoppedEvent, PaneLabel, SourceRowAdapter}
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
    val forDrag = new OnTouchToScrollPane(
      from = PaneLabel.SourceArea,
      onDrag = actions.container.onPaneDragging
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
            direction foreach { dir =>
              val event = PaneDragStoppedEvent(PaneLabel.SourceArea, dir)
              actions.container onPaneDragStopped event
            }
          case _ =>
            forDrag.onTouch(rv, e)
        }
        if (!(previous contains e.getRawX)){
          direction = previous map (e.getRawX - _) map { x =>
            if (x > 0) 1 else -1
          }
          previous = Some(e.getRawX)
        }

      }
      override def onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = {
        Log info s"$e"

        val isHorizontal = detector.onTouchEvent(e)
        if (isHorizontal){
          forDrag.onTouch(rv, e)
        } else {
          forFocus.onTouch(rv, e)
          forDrag.updateCurrentPosition(e.getRawX)
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

private class PaneScrollFilter extends SimpleOnGestureListener {

  private var horizontalCount = 0

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
    horizontalCount = 0
    false
  }
}