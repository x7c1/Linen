package x7c1.linen.modern.accessor

import x7c1.linen.modern.struct.{Date, Entry}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync

import scala.collection.mutable

object EntryLoader {
  def apply(cacher: EntryCacher): EntryLoadExecutor = new EntryLoadExecutor(cacher)
}

class EntryLoader (cacher: EntryCacher, listener: OnEntryLoadedListener){

  def load(sourceId: Long): Unit =
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
      title = s"$sourceId-$n entry " + DummyString.words(10),
      content = s"$sourceId-$n " + DummyString.words(100),
      createdAt = Date.dummy()
    )
  }
}

class EntryLoadExecutor(cacher: EntryCacher){
  def load(sourceId: Long)(f: EntryLoadedEvent => Unit): Unit = {
    val listener = new OnEntryLoadedListener {
      override def onEntryLoaded(e: EntryLoadedEvent): Unit = f(e)
    }
    new EntryLoader(cacher, listener) load sourceId
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

