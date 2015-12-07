package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryBuffer, EntryCacher, EntryLoadedEvent, EntryLoader, OnEntryLoadedListener, SourceAccessor}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class EntryBufferUpdater(
  cacher: EntryCacher,
  entryBuffer: EntryBuffer,
  sourceAccessor: SourceAccessor,
  onEntryLoaded: OnEntryLoadedListener){

  def loadAndInsert(sourceId: Long)(done: EntryBufferInsertedEvent => Unit): Unit = {
    val onLoad = onEntryLoaded append createOnLoadedListener(done)
    new EntryLoader(cacher, onLoad) load sourceId
  }

  private def createOnLoadedListener(done: EntryBufferInsertedEvent => Unit) =
    OnEntryLoadedListener {
      case EntryLoadedEvent(sourceId, entries) =>
        val insert = for {
          position <- task { calculateEntryPositionOf(sourceId) }
          _ <- task of entryBuffer.insertAll(position, sourceId, entries) _
        } yield {
          done(EntryBufferInsertedEvent(position))
        }
        insert.execute()
    }

  private def calculateEntryPositionOf(sourceId: Long): Int = {
    val previousId = sourceAccessor.collectLastFrom(sourceId){
      case source if entryBuffer.has(source.id) =>
        entryBuffer.lastEntryIdOf(source.id)
    }
    entryBuffer positionAfter previousId.flatten
  }

}

case class EntryBufferInsertedEvent (position: Int)
