package x7c1.linen.modern

import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.chrono.BufferingTimer
import x7c1.wheat.modern.kinds.CallbackTask
import x7c1.wheat.modern.kinds.CallbackTask.taskOf

import scala.collection.mutable


class EntryPrefetcher(
  sourceAccessor: SourceAccessor,
  entryPrefetchedListener: OnEntryPrefetchedListener,
  entryCacher: EntryCacher){

  def prefetchAfter(sourceId: Long)(onFinish: EntriesPrefetchTriggered => Unit) = {
    Log info s"[init] sourceId:$sourceId"

    val sources = sourceAccessor.takeAfter(sourceId, 10)
    sources.foreach { source =>
      prefetch(source.id)
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
      entryPrefetchedListener.onEntryPrefetched(new EntriesPrefetchedEvent(sourceId))
    }
  }

}

case class EntriesPrefetchTriggered(
  sourceId: Long
)

trait OnEntryPrefetchedListener {
  def onEntryPrefetched(event: EntriesPrefetchedEvent): Unit
}

class SourceChangedNotifier(
  recyclerView: RecyclerView) extends OnEntryPrefetchedListener {

  import x7c1.wheat.modern.decorator.Imports._

  private val timer = new BufferingTimer(delay = 200)

  override def onEntryPrefetched(event: EntriesPrefetchedEvent): Unit = {
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
