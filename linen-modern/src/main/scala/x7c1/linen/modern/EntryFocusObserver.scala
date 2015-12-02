package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{ItemFocusedEvent, OnItemFocusedListener}

class EntryFocusObserver(
  actions: Actions,
  entryAccessor: EntryAccessor) extends  OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val entry = entryAccessor.get(event.position)
    val e = new EntryFocusedEvent(event.position, entry)
    val sync = for {
      _ <- actions.sourceArea onEntryFocused e
      _ <- actions.entryArea onEntryFocused e
      _ <- actions.detailArea onEntryFocused e
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
