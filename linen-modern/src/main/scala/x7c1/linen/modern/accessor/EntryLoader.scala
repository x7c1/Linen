package x7c1.linen.modern.accessor

import x7c1.linen.modern.struct.Entry
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync.after

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
        after(msec = 500){
          Log info s"[done] source-$sourceId"
          val entries = createDummy(sourceId)
          cacher.updateCache(sourceId, entries)
          listener.onEntryLoaded(new EntryLoadedEvent(sourceId, entries))
        }
    }

  def createDummy(sourceId: Long) = DummyCreator.createEntriesOf(sourceId)
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

