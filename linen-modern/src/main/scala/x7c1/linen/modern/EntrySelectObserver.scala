package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log

class EntrySelectObserver(actions: Actions) extends OnEntrySelectedListener {
  override def onEntrySelected(event: EntrySelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val x1 = for {
      _ <- actions.entryArea onEntrySelected event
      _ <- actions.container onEntrySelected event
    } yield ()

    val x2 = for {
      _ <- actions.sourceArea onEntrySelected event
      _ <- actions.detailArea onEntrySelected event
    } yield ()

    Seq(x1, x2) foreach runAsync { Log error _.toString }
  }
}
