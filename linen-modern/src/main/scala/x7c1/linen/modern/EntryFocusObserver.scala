package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{ItemFocusedEvent, OnItemFocusedListener}

class EntryFocusObserver(
  entryAccessor: EntryAccessor,
  observerTasks: EntryRowObserverTasks) extends  OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val entry = entryAccessor.get(event.position)
    val tasks = observerTasks.commonTo(entry.sourceId, event.position)
    tasks foreach runAsync { Log error _.toString }
  }
}
