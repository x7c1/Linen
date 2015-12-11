package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.{Actions, EntryFocusedEvent}
import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.display.{EntrySelectedEvent, OnEntrySelectedListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.OnItemFocusedListener

class EntryFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[EntryFocusedEvent] {

  override def onFocused(event: EntryFocusedEvent): Unit = {
    val sync = for {
      _ <- actions.sourceArea onEntryFocused event
      _ <- actions.entryArea onEntryFocused event
      _ <- actions.detailArea onEntryFocused event
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
