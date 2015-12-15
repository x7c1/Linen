package x7c1.linen.modern.init

import java.lang.Math.max

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import x7c1.linen.glue.res.layout.{EntryDetailRow, EntryRow, MainLayout, SourceRow}
import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.action.observer.{SourceSkipDoneObserver, EntryDetailFocusedObserver, EntryDetailSelectedObserver, EntryFocusedObserver, EntrySelectedObserver, SourceFocusedObserver, SourceSelectedObserver, SourceSkippedObserver}
import x7c1.linen.modern.action.{SourceSkipDoneFactory, Actions, ContainerAction, EntryAreaAction, EntryDetailAreaAction, EntryDetailFocusedEventFactory, EntryFocusedEventFactory, SourceAreaAction, SourceFocusedEventFactory, SourceSkippedEventFactory}
import x7c1.linen.modern.display.{EntryArea, EntryDetailArea, EntryDetailRowAdapter, EntryRowAdapter, PaneContainer, SourceArea, SourceRowAdapter}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.observer.{SourceSkippedDetector, FocusDetector}
import x7c1.wheat.modern.tasks.Async.await
import x7c1.wheat.modern.tasks.UiThread


class ContainerInitializer(
  activity: Activity,
  layout: MainLayout,
  sourceRowProvider: ViewHolderProvider[SourceRow],
  entryRowProvider: ViewHolderProvider[EntryRow],
  entryDetailRowProvider: ViewHolderProvider[EntryDetailRow]) {

  def setup(): Unit = {
    updateWidth(0.85, layout.sourceArea)
    updateWidth(0.9, layout.entryArea)
    updateWidth(0.95, layout.entryDetailArea)

    DummyFactory.setup(layout, activity)

    init.execute()
  }
  private def init = for {
    _ <- await(0)
    accessors <- task apply createAccessors
    actions <- task {
      createActions(accessors)
    }
    _ <- UiThread.via(layout.itemView){ _ =>
      setupSourceArea(actions, accessors)
      setupEntryArea(actions, accessors)
      setupEntryDetailArea(actions, accessors)
    }
  } yield ()

  private def createAccessors = {

    // todo: db.close

    new Accessors(
      source = SourceAccessor create activity,
      entryOutline = EntryAccessor forEntryOutline activity,
      entryDetail = EntryAccessor forEntryDetail activity
    )
  }

  private def setupSourceArea(actions: Actions, accessors: Accessors) = {
    val manager = new LinearLayoutManager(activity)
    layout.sourceList setLayoutManager manager
    layout.sourceList setAdapter new SourceRowAdapter(
      accessors.source,
      new SourceSelectedObserver(actions),
      sourceRowProvider
    )
    layout.sourceList setOnTouchListener FocusDetector.createListener(
      recyclerView = layout.sourceList,
      getPosition = () => manager.findFirstCompletelyVisibleItemPosition(),
      focusedEventFactory = new SourceFocusedEventFactory(accessors.source),
      onFocused = new SourceFocusedObserver(actions)
    )
    layout.sourceToNext setOnTouchListener SourceSkippedDetector.createListener(
      context = layout.sourceToNext.getContext,
      getCurrentPosition = () => {
        manager.findFirstCompletelyVisibleItemPosition() match {
          case x if x < 0 => None
          case x => Some(x)
        }
      },
      getNextPosition = () => Some {
        manager.findFirstCompletelyVisibleItemPosition() + 1
      },
      skippedEventFactory = new SourceSkippedEventFactory(accessors.source),
      skipDoneEventFactory = new SourceSkipDoneFactory(accessors.source),
      onSkippedListener = new SourceSkippedObserver(actions),
      onSkipDoneListener = new SourceSkipDoneObserver(actions)
    )
  }
  private def setupEntryArea(actions: Actions, accessors: Accessors) = {
    val manager = new LinearLayoutManager(activity)
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
  }
  private def setupEntryDetailArea(actions: Actions, accessors: Accessors) = {
    val manager = new LinearLayoutManager(activity)
    val getPosition = () => {
      manager.findFirstCompletelyVisibleItemPosition() match {
        case n if n < 0 => manager.findFirstVisibleItemPosition()
        case n => n
      }
    }
    layout.entryDetailList setLayoutManager manager
    layout.entryDetailList setAdapter new EntryDetailRowAdapter(
      accessors.entryDetail,
      new EntryDetailSelectedObserver(actions),
      entryDetailRowProvider
    )
    layout.entryDetailList setOnTouchListener FocusDetector.createListener(
      recyclerView = layout.entryDetailList,
      getPosition = getPosition,
      focusedEventFactory = new EntryDetailFocusedEventFactory(accessors.entryDetail),
      onFocused = new EntryDetailFocusedObserver(actions)
    )
  }

  private lazy val displaySize: Point = {
    val display = activity.getWindowManager.getDefaultDisplay
    val size = new Point
    display getSize size
    size
  }

  private def createActions(accessors: Accessors) = {
    val panePosition = {
      val length = layout.paneContainer.getChildCount
      val children = 0 to (length - 1) map layout.paneContainer.getChildAt
      new PanePosition(children, displaySize.x)
    }
    val sourceArea = new SourceArea(
      sources = accessors.source,
      recyclerView = layout.sourceList,
      getPosition = () => panePosition of layout.sourceArea
    )
    val entryArea = new EntryArea(
      toolbar = layout.entryToolbar,
      recyclerView = layout.entryList,
      getPosition = () => panePosition of layout.entryArea
    )
    val entryDetailArea = new EntryDetailArea(
      toolbar = layout.entryDetailToolbar,
      recyclerView = layout.entryDetailList,
      getPosition = () => panePosition of layout.entryDetailArea
    )
    new Actions(
      new ContainerAction(
        container = new PaneContainer(layout.paneContainer),
        entryArea,
        entryDetailArea
      ),
      new SourceAreaAction(sourceArea, accessors.source),
      new EntryAreaAction(
        entryArea = entryArea,
        sourceAccessor = accessors.source,
        entryAccessor = accessors.entryOutline
      ),
      new EntryDetailAreaAction(entryDetailArea, accessors.entryDetail)
    )
  }

  private def updateWidth(ratio: Double, view: View): Unit = {
    val params = view.getLayoutParams
    params.width = (ratio * displaySize.x).toInt
    view setLayoutParams params
  }
}

class Accessors(
  val source: SourceAccessor,
  val entryOutline: EntryAccessor[EntryOutline],
  val entryDetail: EntryAccessor[EntryDetail]
)

private class PanePosition(children: Seq[View], displayWidth: Int){
  def of(view: View): Int = {
    positions find (_._1 == view) map (_._2) getOrElse {
      throw new IllegalStateException("view not found")
    }
  }
  private lazy val positions = {
    val xs = children.scanLeft(0){_ + _.getWidth}
    children.zip(xs).zipWithIndex map { case ((view, start), i) =>
      val position =
        if (i == children.length - 1)
          start - (displayWidth - view.getWidth)
        else
          start - (displayWidth - view.getWidth) / 2

      view -> max(0, position)
    }
  }
}
