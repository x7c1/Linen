package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.OnItemFocusedListener

class SourceFocusedObserver(actions: Actions)
  extends OnItemFocusedListener[SourceFocusedEvent] {

  override def onFocused(event: SourceFocusedEvent): Unit = {
    Log info s"[init] ${event.position}"

    val sync = for {
      _ <- actions.entryArea onSourceFocused event
      _ <- actions.detailArea onSourceFocused event
    } yield {
      Log debug s"[ok] focus on source.id:${event.source.id}"
    }
    Seq(
      sync,
      actions.prefetcher onSourceFocused event
    ) foreach runAsync { Log error _.toString }
  }
}
