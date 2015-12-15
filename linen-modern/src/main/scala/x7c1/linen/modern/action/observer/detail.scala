package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.action.{EntrySkipDone, EntrySkippedEvent, Actions, EntryDetailFocusedEvent}
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, OnEntryDetailSelectedListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{OnSkipDoneListener, OnItemSkippedListener, OnItemFocusedListener}


class EntryDetailFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[EntryDetailFocusedEvent]{

  override def onFocused(event: EntryDetailFocusedEvent): Unit = {
    val sync = for {
      _ <- actions.detailArea onEntryDetailFocused event
      _ <- actions.entryArea onEntryDetailFocused event
      _ <- actions.sourceArea onEntryDetailFocused event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class EntryDetailSelectedObserver(actions: Actions)
  extends OnEntryDetailSelectedListener {

  override def onEntryDetailSelected(event: EntryDetailSelectedEvent): Unit = {
    val sync = for {
      _ <- actions.detailArea onEntryDetailSelected event
      _ <- actions.entryArea onEntryDetailSelected event
      _ <- actions.sourceArea onEntryDetailSelected event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class EntryDetailSkippedObserver(actions: Actions)
  extends OnItemSkippedListener[EntrySkippedEvent] {

  override def onSkipped(event: EntrySkippedEvent) = {
    val sync = for {
      _ <- actions.detailArea onEntryDetailSkipped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class EntryDetailSkipDoneObserver(actions: Actions)
  extends OnSkipDoneListener[EntrySkipDone]{

  override def onSkipDone(event: EntrySkipDone) = {
    val sync = for {
      _ <- actions.entryArea onEntryDetailSkipDone event
      _ <- actions.sourceArea onEntryDetailSkipDone event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
