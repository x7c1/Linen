package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.{SourceSkippedEvent, Actions, SourceFocusedEvent}
import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.display.{OnSourceSelectedListener, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.OnItemFocusedListener


class SourceFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[SourceFocusedEvent] {

  override def onFocused(event: SourceFocusedEvent): Unit = {
    val sync = for {
      _ <- actions.entryArea onSourceFocused event
      _ <- actions.detailArea onSourceFocused event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class SourceSelectedObserver(actions: Actions)
  extends OnSourceSelectedListener {

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    val sync = for {
      _ <- actions.entryArea onSourceSelected event
      _ <- actions.sourceArea onSourceSelected event
      _ <- actions.container onSourceSelected event
      _ <- actions.detailArea onSourceSelected event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class SourceSkippedObserver(actions: Actions)
  extends OnItemSkippedListener {

  override def onSkipped(event: SourceSkippedEvent): Unit = {
    val sync = for {
      _ <- actions.sourceArea onSourceSkipped event
      _ <- actions.entryArea onSourceSkipped event
      _ <- actions.detailArea onSourceSkipped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}