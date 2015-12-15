package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.{EntrySkipStopped, EntrySkippedEvent, Actions, EntryFocusedEvent}
import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.display.{EntrySelectedEvent, OnEntrySelectedListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{OnSkipStoppedListener, OnItemSkippedListener, OnItemFocusedListener}

class EntryFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[EntryFocusedEvent] {

  override def onFocused(event: EntryFocusedEvent): Unit = {
    val sync = for {
      _ <- actions.detailArea onEntryFocused event
      _ <- actions.entryArea onEntryFocused event
      _ <- actions.sourceArea onEntryFocused event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class EntrySelectedObserver(actions: Actions) extends OnEntrySelectedListener {
  override def onEntrySelected(event: EntrySelectedEvent): Unit = {
    val sync = for {
      _ <- actions.detailArea onEntrySelected event
      _ <- actions.entryArea onEntrySelected event
      _ <- actions.container onEntrySelected event
      _ <- actions.sourceArea onEntrySelected event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class EntrySkippedObserver(actions: Actions)
  extends OnItemSkippedListener[EntrySkippedEvent] {

  override def onSkipped(event: EntrySkippedEvent) = {
    val sync = for {
      _ <- actions.entryArea onEntrySkipped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class EntrySkipStoppedObserver(actions: Actions)
  extends OnSkipStoppedListener[EntrySkipStopped]{

  override def onSkipStopped(event: EntrySkipStopped) = {
    val sync = for {
      _ <- actions.detailArea onEntrySkipStopped event
      _ <- actions.sourceArea onEntrySkipStopped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
