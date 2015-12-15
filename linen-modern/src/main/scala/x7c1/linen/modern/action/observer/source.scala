package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.{SourceSkipStopped, SourceSkippedEvent, Actions, SourceFocusedEvent}
import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.display.{OnSourceSelectedListener, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{OnSkipStoppedListener, OnItemSkippedListener, OnItemFocusedListener}


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
  extends OnItemSkippedListener[SourceSkippedEvent] {

  override def onSkipped(event: SourceSkippedEvent): Unit = {
    val sync = for {
      _ <- actions.sourceArea onSourceSkipped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class SourceSkipStoppedObserver(actions: Actions)
  extends OnSkipStoppedListener[SourceSkipStopped] {

  override def onSkipStopped(event: SourceSkipStopped): Unit = {
    Log error "init"
    val sync = for {
      _ <- actions.entryArea onSourceSkipStopped event
      _ <- actions.detailArea onSourceSkipStopped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
