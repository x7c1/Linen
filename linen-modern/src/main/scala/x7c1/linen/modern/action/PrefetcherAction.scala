package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryPrefetcher, SourceAccessor}
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.OnFinish
import x7c1.wheat.modern.patch.TaskAsync.async

class PrefetcherAction(
  prefetcher: EntryPrefetcher,
  sourceAccessor: SourceAccessor,
  entryBufferUpdater: EntryBufferUpdater
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipped
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected with OnEntryDetailFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    load(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    load(event.source.id)
  }
  override def onSourceSkipped(event: SourceSkippedEvent) = {
    load(event.nextSource.id)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    load(event.entry.sourceId)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    load(event.entry.sourceId)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = {
    load(event.entry.sourceId)
  }
  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = {
    load(event.entry.sourceId)
  }

  private def load(sourceId: Long) = for {
    _ <- task { prefetcher triggerBy sourceId }
    _ <- task { insertAfter(sourceId) }
  } yield ()

  private def insertAfter(sourceId: Long): Unit = async {
    sourceAccessor.findNextId(sourceId) foreach { sourceId =>
      entryBufferUpdater.loadAndInsert(sourceId)(OnFinish.nop)
    }
  }

}
