package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.action.{Actions, OutlineFocusedEvent, EntrySkipStopped, EntrySkippedEvent}
import x7c1.linen.modern.display.unread.{OutlineSelectedEvent, OnOutlineSelectedListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{OnItemFocusedListener, OnItemSkippedListener, OnSkipStoppedListener}

class OutlineFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[OutlineFocusedEvent] {

  override def onFocused(event: OutlineFocusedEvent): Unit = {
    val sync = for {
      _ <- actions.detailArea onOutlineFocused event
      _ <- actions.outlineArea onOutlineFocused event
      _ <- actions.sourceArea onOutlineFocused event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class OutlineSelectedObserver(actions: Actions) extends OnOutlineSelectedListener {
  override def onEntrySelected(event: OutlineSelectedEvent): Unit = {
    val sync = for {
      _ <- actions.detailArea onOutlineSelected event
      _ <- actions.outlineArea onOutlineSelected event
      _ <- actions.container onOutlineSelected event
      _ <- actions.sourceArea onOutlineSelected event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class OutlineSkippedObserver(actions: Actions)
  extends OnItemSkippedListener[EntrySkippedEvent] {

  override def onSkipped(event: EntrySkippedEvent) = {
    val sync = for {
      _ <- actions.outlineArea onOutlineSkipped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}

class OutlineSkipStoppedObserver(actions: Actions)
  extends OnSkipStoppedListener[EntrySkipStopped]{

  override def onSkipStopped(event: EntrySkipStopped) = {
    val sync = for {
      _ <- actions.detailArea onOutlineSkipStopped event
      _ <- actions.sourceArea onOutlineSkipStopped event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
