package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.action.{Actions, DetailFocusedEvent, EntrySkipStopped, EntrySkippedEvent}
import x7c1.linen.modern.display.unread.{DetailSelectedEvent, OnDetailSelectedListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{OnItemFocusedListener, OnItemSkippedListener, OnSkipStoppedListener}


class EntryDetailFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[DetailFocusedEvent]{

  override def onFocused(event: DetailFocusedEvent): Unit = {
    val sync = for {
      _ <- actions.detailArea onDetailFocused event
      _ <- actions.outlineArea onDetailFocused event
      _ <- actions.sourceArea onDetailFocused event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class DetailSelectedObserver(actions: Actions)
  extends OnDetailSelectedListener {

  override def onEntryDetailSelected(event: DetailSelectedEvent): Unit = {
    val sync = for {
      _ <- actions.detailArea onDetailSelected event
      _ <- actions.outlineArea onDetailSelected event
      _ <- actions.sourceArea onDetailSelected event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class DetailSkippedObserver(actions: Actions)
  extends OnItemSkippedListener[EntrySkippedEvent] {

  override def onSkipped(event: EntrySkippedEvent) = {
    val sync = for {
      _ <- actions.detailArea onDetailSkipped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class EntryDetailSkipStoppedObserver(actions: Actions)
  extends OnSkipStoppedListener[EntrySkipStopped]{

  override def onSkipStopped(event: EntrySkipStopped) = {
    val sync = for {
      _ <- actions.outlineArea onDetailSkipStopped event
      _ <- actions.sourceArea onDetailSkipStopped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
