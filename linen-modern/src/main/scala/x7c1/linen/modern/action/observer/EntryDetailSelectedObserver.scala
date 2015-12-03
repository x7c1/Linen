package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.Actions
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, OnEntryDetailSelectedListener}
import x7c1.wheat.macros.logger.Log

class EntryDetailSelectedObserver(actions: Actions)
  extends OnEntryDetailSelectedListener {

  import CallbackTaskRunner.runAsync

  override def onEntryDetailSelected(event: EntryDetailSelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val sync = for {
      _ <- actions.detailArea.onEntryDetailSelected(event)
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
