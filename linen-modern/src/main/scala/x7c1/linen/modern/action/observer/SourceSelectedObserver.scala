package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.Actions
import x7c1.linen.modern.action.observer.CallbackTaskRunner.runAsync
import x7c1.linen.modern.display.{OnSourceSelectedListener, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log

class SourceSelectedObserver(actions: Actions) extends OnSourceSelectedListener {

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val sync = for {
      _ <- actions.entryArea onSourceSelected event
      _ <- actions.sourceArea onSourceSelected event
      _ <- actions.container onSourceSelected event
      _ <- actions.detailArea onSourceSelected event
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }

}
