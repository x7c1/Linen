package x7c1.linen.modern

import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.logger.Log

import scala.collection.mutable


class EntryPrefetcher(
  sourceAccessor: SourceAccessor,
  entryPrefetchedListener: OnPrefetchDoneListener,
  entryCacher: EntryCacher){

  def triggerBy(sourceId: Long)(onFinish: EntriesPrefetchTriggered => Unit) = {
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

    EntryLoader(entryCacher).load(sourceId){ _ =>
      prefetching(sourceId) = false
      Log debug s"[done] source-$sourceId"
      entryPrefetchedListener onPrefetchDone new PrefetchDoneEvent(sourceId)
    }
  }

}

case class EntriesPrefetchTriggered(
  sourceId: Long
)

trait OnPrefetchDoneListener { self =>

  def onPrefetchDone(event: PrefetchDoneEvent): Unit

  def append(listener: OnPrefetchDoneListener): OnPrefetchDoneListener = {
    new OnPrefetchDoneListener {
      override def onPrefetchDone(event: PrefetchDoneEvent): Unit = {
        self onPrefetchDone event
        listener onPrefetchDone event
      }
    }
  }
}

class SourceStateUpdater(
  sourceStateBuffer: SourceStateBuffer) extends OnPrefetchDoneListener {

  override def onPrefetchDone(event: PrefetchDoneEvent): Unit = {
    sourceStateBuffer.updateState(event.sourceId, SourcePrefetched)
  }
}

class SourceChangedNotifier(
  sourceAccessor: SourceAccessor,
  recyclerView: RecyclerView) extends OnPrefetchDoneListener {

  import x7c1.wheat.modern.decorator.Imports._

  override def onPrefetchDone(event: PrefetchDoneEvent): Unit = {
    recyclerView runUi { view =>
      val position = sourceAccessor.positionOf(event.sourceId)
      view.getAdapter.notifyItemChanged(position)
    }
  }
}

case class PrefetchDoneEvent(
  sourceId: Long
)
