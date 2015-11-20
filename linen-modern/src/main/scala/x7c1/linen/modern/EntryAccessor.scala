package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait EntryAccessor {
  def get: Seq[Entry]
}

class EntryBuffer extends EntryAccessor {

  private val underlying = ListBuffer[Entry]()

  override def get: Seq[Entry] = underlying

  def indexOf(entryId: Long): Int =
    underlying.indexWhere(_.entryId == entryId)

  def positionAfter(entryId: Option[Long]) = {
    entryId match {
      case Some(id) => underlying.indexWhere(_.entryId == id) + 1
      case _ => 0
    }
  }
  def insertAll(position: Int, entries: Seq[Entry]) = {
    underlying.insertAll(position, entries)
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

class EntryLoader (cacher: EntryCacher, listener: OnEntriesLoadedListener){

  def load(sourceId: Long) =
    cacher.findCache(sourceId) match {
      case Some(entries) => listener.onEntryLoaded(new EntriesLoadedEvent(entries))
      case None =>
        TaskAsync.run(delay = 500){
          Log info s"[done] source-$sourceId"
          val entries = createDummy(sourceId)
          cacher.updateCache(sourceId, entries)
          listener.onEntryLoaded(new EntriesLoadedEvent(entries))
        }
    }

  def createDummy(sourceId: Long) = (1 to 50) map { n =>
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
  def load(sourceId: Long)(f: EntriesLoadedEvent => Unit) = {
    val listener = new OnEntriesLoadedListener {
      override def onEntryLoaded(e: EntriesLoadedEvent): Unit = f(e)
    }
    new EntryLoader(cacher, listener) load sourceId
  }
}

sealed trait EntryLoaderEvent

case class EntriesLoadedEvent(
  entries: Seq[Entry] ) extends EntryLoaderEvent

trait OnEntriesLoadedListener {
  def onEntryLoaded(e: EntriesLoadedEvent): Unit
}
