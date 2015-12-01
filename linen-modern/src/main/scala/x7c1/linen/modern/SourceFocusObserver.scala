package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{ItemFocusedEvent, OnItemFocusedListener}

class SourceFocusObserver(
  sourceAccessor: SourceAccessor,
  observerTasks: SourceRowObserverTasks) extends OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val source = sourceAccessor get event.position
    val focus = for {
      _ <- observerTasks.displayEntryArea(source.id)
      _ <- observerTasks.updateEntryDetailArea(source.id)
    } yield {
      Log debug s"[ok] focus on source.id:${source.id}"
    }
    val tasks = Seq(focus, observerTasks.prefetch(source.id))
    tasks foreach runAsync { Log error _.toString }
  }
}
