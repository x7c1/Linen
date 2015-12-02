package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.OnItemFocusedListener

class EntryFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[EntryFocusedEvent] {

  override def onFocused(event: EntryFocusedEvent): Unit = {
    Log info s"[init] ${event.position}"

    val sync = for {
      _ <- actions.sourceArea onEntryFocused event
      _ <- actions.entryArea onEntryFocused event
      _ <- actions.detailArea onEntryFocused event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
