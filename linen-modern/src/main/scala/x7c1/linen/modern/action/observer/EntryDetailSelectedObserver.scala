package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.action.{EntryDetailFocusedEvent, Actions}
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, OnEntryDetailSelectedListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.OnItemFocusedListener

class EntryDetailSelectedObserver(actions: Actions)
  extends OnEntryDetailSelectedListener {

  import CallbackTaskRunner.runAsync

  override def onEntryDetailSelected(event: EntryDetailSelectedEvent): Unit = {
    Log info s"[init] ${event.position}"

    val sync = for {
      _ <- actions.detailArea onEntryDetailSelected event
      _ <- actions.entryArea onEntryDetailSelected event
      _ <- actions.sourceArea onEntryDetailSelected event
      _ <- actions.prefetcher onEntryDetailSelected event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class EntryDetailFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[EntryDetailFocusedEvent]{

  override def onFocused(event: EntryDetailFocusedEvent): Unit = {
    Log info s"[init] ${event.position}"

    val sync = for {
      _ <- actions.detailArea onEntryDetailFocused event
      _ <- actions.entryArea onEntryDetailFocused event
      _ <- actions.sourceArea onEntryDetailFocused event
      _ <- actions.prefetcher onEntryDetailFocused event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
