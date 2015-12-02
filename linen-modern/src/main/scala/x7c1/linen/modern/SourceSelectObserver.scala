package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log

class SourceSelectObserver(actions: Actions) extends OnSourceSelectedListener {

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val sync = for {
      _ <- actions.entryArea onSourceSelected event
      _ <- actions.sourceArea onSourceSelected event
      _ <- actions.container onSourceSelected event
      _ <- actions.detailArea onSourceSelected event
    } yield {
      Log debug s"[ok] select source-${event.source.id}"
    }
    Seq(
      sync,
      actions.prefetcher onSourceSelected event
    ) foreach runAsync { Log error _.toString }
  }

}
