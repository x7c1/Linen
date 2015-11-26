package x7c1.linen.modern

import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait EntryAccessor {

  def get(position: Int): Entry

  def length: Int
}

class EntryBuffer extends EntryAccessor {

  private val underlying = ListBuffer[Entry]()

  override def get(position: Int): Entry = underlying(position)

  override def length = underlying.length

  def indexOf(entryId: Long): Int =
    underlying.indexWhere(_.entryId == entryId)

  def positionAfter(entryId: Option[Long]) = {
    entryId match {
      case Some(id) => underlying.indexWhere(_.entryId == id) + 1
      case _ => 0
    }
  }
  def insertAll(position: Int, sourceId: Long, entries: Seq[Entry]): Seq[Entry] = {
    val newer = entries filterNot { this has _.sourceId }
    underlying.insertAll(position, newer)
    entriesMapping(sourceId) = entries.map(_.entryId)
    newer
  }

  private lazy val entriesMapping = mutable.Map[Long, Seq[Long]]()

  def has(sourceId: Long): Boolean = {
    entriesMapping.get(sourceId).exists(_.nonEmpty)
  }
  def firstEntryIdOf(sourceId: Long): Option[Long] = {
    entriesMapping.get(sourceId).flatMap(_.headOption)
  }
  def lastEntryIdOf(sourceId: Long): Option[Long] = {
    entriesMapping.get(sourceId).flatMap(_.lastOption)
  }

}

class EntryCacher {

  private val cache = new mutable.HashMap[Long, Seq[Entry]]

  def findCache(sourceId: Long): Option[Seq[Entry]] = {
    cache.get(sourceId)
  }
  def updateCache(sourceId: Long, entries: Seq[Entry]) = {
    cache(sourceId) = entries
  }
}

class EntryLoader (cacher: EntryCacher, listener: OnEntryLoadedListener){

  def load(sourceId: Long) =
    cacher.findCache(sourceId) match {
      case Some(entries) =>
        listener.onEntryLoaded(new EntryLoadedEvent(sourceId, entries))
      case None =>
        TaskAsync.run(delay = 500){
          Log info s"[done] source-$sourceId"
          val entries = createDummy(sourceId)
          cacher.updateCache(sourceId, entries)
          listener.onEntryLoaded(new EntryLoadedEvent(sourceId, entries))
        }
    }

  def createDummy(sourceId: Long) = (1 to 10) map { n =>
    Entry(
      sourceId = sourceId,
      entryId = sourceId * 1000 + n,
      url = s"http://example.com/source-$sourceId/entry-$n",
      title = s"$sourceId-$n entry",
      content = s"sample content $sourceId-$n " * 5,
      createdAt = LinenDate.dummy()
    )
  }
}

object EntryLoader {
  def apply(cacher: EntryCacher): EntryLoadExecutor = new EntryLoadExecutor(cacher)
}

class EntryLoadExecutor(cacher: EntryCacher){
  def load(sourceId: Long)(f: EntryLoadedEvent => Unit) = {
    val listener = new OnEntryLoadedListener {
      override def onEntryLoaded(e: EntryLoadedEvent): Unit = f(e)
    }
    new EntryLoader(cacher, listener) load sourceId
  }
}

class SourceStateUpdater(
  sourceStateBuffer: SourceStateBuffer) extends OnEntryLoadedListener {

  override def onEntryLoaded(event: EntryLoadedEvent): Unit = {
    sourceStateBuffer.updateState(event.sourceId, SourcePrefetched)
  }
}

class SourceChangedNotifier(
  sourceAccessor: SourceAccessor,
  recyclerView: RecyclerView) extends OnEntryLoadedListener {

  import x7c1.wheat.modern.decorator.Imports._

  override def onEntryLoaded(event: EntryLoadedEvent): Unit = {
    recyclerView runUi { view =>
      sourceAccessor.positionOf(event.sourceId).
        foreach(view.getAdapter.notifyItemChanged)

    }
  }
}
