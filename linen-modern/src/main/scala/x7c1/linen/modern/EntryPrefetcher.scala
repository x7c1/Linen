package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.kinds.CallbackTask
import x7c1.wheat.modern.kinds.CallbackTask.taskOf


class EntryPrefetcher(
  sourceAccessor: SourceAccessor,
  entryLoader: EntryLoader){

  def prefetchAfter(sourceId: Long)(onFinish: EntriesPrefetchTriggered => Unit) = {
    Log info s"[init] sourceId:$sourceId"

    val sources = sourceAccessor.takeAfter(sourceId, 10)
    sources.foreach { source =>
      entryLoader.prefetch(source.id)(onPrefetchComplete)
    }
    onFinish(new EntriesPrefetchTriggered(sourceId))
  }

  def createTaskOf(sourceId: Long): CallbackTask[Unit] = {
    for {
      _ <- taskOf(prefetchAfter(sourceId))
    } yield {
        Log info s"[done] prefetch triggered around sourceId:$sourceId"
    }
  }

  def onPrefetchComplete(event: EntriesPrefetchTriggered): Unit = {
    Log verbose s"[init] sourceId:${event.sourceId}"
  }
}
