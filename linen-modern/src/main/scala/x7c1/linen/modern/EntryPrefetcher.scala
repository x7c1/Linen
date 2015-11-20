package x7c1.linen.modern

import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.chrono.BufferingTimer

import scala.collection.mutable


class EntryPrefetcher(
  sourceAccessor: SourceAccessor,
  entryPrefetchedListener: OnPrefetchDoneListener,
  entryCacher: EntryCacher){

  def startFrom(sourceId: Long)(onFinish: EntriesPrefetchTriggered => Unit) = {
    Log info s"[init] sourceId:$sourceId"

    val sources = sourceAccessor.takeAfter(sourceId, 10)
    sources.foreach { source =>
      prefetch(source.id)
    }
    onFinish(new EntriesPrefetchTriggered(sourceId))
  }

  private val prefetching = mutable.Map[Long, Boolean]()

  def prefetch(sourceId: Long): Unit = {
    if (entryCacher.findCache(sourceId).nonEmpty){
      Log info s"[cancel] source-$sourceId already cached"
      return
    }
    if (prefetching.getOrElse(sourceId, false)){
      Log info s"[cancel] source-$sourceId prefetching already started"
      return
    }
    prefetching(sourceId) = true

    EntryLoader(entryCacher).load(sourceId){ e =>
      prefetching(sourceId) = false
      Log debug s"[done] source-$sourceId"
      entryPrefetchedListener onPrefetchDone new EntriesPrefetchedEvent(sourceId)
    }
  }

}

case class EntriesPrefetchTriggered(
  sourceId: Long
)

trait OnPrefetchDoneListener {
  def onPrefetchDone(event: EntriesPrefetchedEvent): Unit
}

class SourceChangedNotifier(
  recyclerView: RecyclerView) extends OnPrefetchDoneListener {

  import x7c1.wheat.modern.decorator.Imports._

  private val timer = new BufferingTimer(delay = 200)

  override def onPrefetchDone(event: EntriesPrefetchedEvent): Unit = {
    /*
    update
     */
    timer touch {
      Log error s"${event.sourceId}"
      recyclerView runUi { _.getAdapter.notifyDataSetChanged() }
    }
  }
}

case class EntriesPrefetchedEvent(
  sourceId: Long
)
