package x7c1.linen.modern

import x7c1.linen.modern.CallbackTaskRunner.runAsync
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class EntrySelectObserver(
  container: PaneContainer,
  observerTasks: EntryRowObserverTasks ) extends OnEntrySelectedListener {

  override def onEntrySelected(event: EntrySelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val scrollEntry = for {
      _ <- task of container.entryArea.scrollTo(event.position) _
      _ <- task of container.scrollTo(container.entryDetailArea)
    } yield {
      Log info s"[ok] entry scrolled to position:${event.position}"
    }
    val tasks = observerTasks.commonTo(event.entry.sourceId, event.position) :+ scrollEntry
    tasks foreach runAsync { Log error _.toString }
  }
}
