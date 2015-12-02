package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import scala.collection.mutable


class EntryPrefetcher(
  sourceAccessor: SourceAccessor,
  entryLoadedListener: OnEntryLoadedListener,
  entryCacher: EntryCacher){

  def triggerBy(sourceId: Long): Unit = {
    Log info s"[init] sourceId:$sourceId"

    val sources = sourceAccessor.takeAfter(sourceId, 10)
    sources.foreach { source =>
      prefetch(source.id)
    }
  }

  private val prefetching = mutable.Map[Long, Boolean]()

  def prefetch(sourceId: Long): Unit = {
    if (entryCacher.findCache(sourceId).nonEmpty){
      Log verbose s"[cancel] source-$sourceId already cached"
      return
    }
    if (prefetching.getOrElse(sourceId, false)){
      Log info s"[cancel] source-$sourceId prefetching already started"
      return
    }
    prefetching(sourceId) = true

    EntryLoader(entryCacher).load(sourceId){ event =>
      prefetching(sourceId) = false
      Log debug s"[done] source-$sourceId"
      entryLoadedListener onEntryLoaded event
    }
  }

}
