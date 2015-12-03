package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.Actions
import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.display.{EntrySelectedEvent, OnEntrySelectedListener}
import x7c1.wheat.macros.logger.Log

class EntrySelectedObserver(actions: Actions) extends OnEntrySelectedListener {
  override def onEntrySelected(event: EntrySelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val sync = for {
      _ <- actions.detailArea onEntrySelected event
      _ <- actions.entryArea onEntrySelected event
      _ <- actions.container onEntrySelected event
      _ <- actions.sourceArea onEntrySelected event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
