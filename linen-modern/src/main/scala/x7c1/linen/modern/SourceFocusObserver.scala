package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.{ItemFocusedEvent, OnItemFocusedListener}

class SourceFocusObserver(
  actions: Actions, sourceAccessor: SourceAccessor) extends OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val source = sourceAccessor get event.position
    val focusedEvent = SourceFocusedEvent(event.position, source)
    val sync = for {
      _ <- actions.entryArea onSourceFocused focusedEvent
      _ <- actions.detailArea onSourceFocused focusedEvent
    } yield {
      Log debug s"[ok] focus on source.id:${source.id}"
    }
    Seq(
      sync,
      actions.prefetcher onSourceFocused focusedEvent
    ) foreach runAsync { Log error _.toString }
  }
}
