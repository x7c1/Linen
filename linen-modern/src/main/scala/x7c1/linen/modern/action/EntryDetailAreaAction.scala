package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{RawSourceAccessor, UnreadSourceAccessor, EntryAccessor}
import x7c1.linen.modern.display.{EntryDetailArea, EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.EntryDetail
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.Async.await

class EntryDetailAreaAction(
  entryDetailArea: EntryDetailArea,
  sourceAccessor: UnreadSourceAccessor,
  rawSourceAccessor: RawSourceAccessor,
  entryAccessor: EntryAccessor[EntryDetail]
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipStopped
  with OnEntrySelected with OnEntryFocused with OnEntrySkipStopped
  with OnEntryDetailSelected with OnEntryDetailFocused with OnEntryDetailSkipped {

  override def onSourceSelected(event: SourceSelectedEvent) = for {
    _ <- skipTo(event.source.id)
  } yield ()

  override def onSourceFocused(event: SourceFocusedEvent) = for {
    _ <- await(100)
    _ <- skipTo(event.source.id)
  } yield()

  override def onSourceSkipStopped(event: SourceSkipStopped) = for {
    _ <- await(150)
    Some(entryPosition) <- task {
      entryAccessor firstEntryPositionOf event.currentSource.id
    }
    Some(entry) <- task {
      entryAccessor findAt entryPosition
    }
    _ <- skipTo(entryPosition, entry.sourceId)
  } yield ()

  override def onEntrySelected(event: EntrySelectedEvent) = {
    fastScrollTo(event.position, event.entry.sourceId)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    fastScrollTo(event.position, event.entry.sourceId)
  }
  override def onEntrySkipStopped(event: EntrySkipStopped) = {
    skipTo(event.currentPosition, event.currentEntry.sourceId)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = for {
    _ <- entryDetailArea scrollTo event.position
    _ <- updateToolbar(event.entry.sourceId)
  } yield ()

  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) =
    updateToolbar(event.entry.sourceId)

  override def onEntryDetailSkipped(event: EntrySkippedEvent) = {
    skipTo(event.nextPosition, event.nextEntry.sourceId)
  }

  private def skipTo(position: Int, sourceId: Long) = for {
    _ <- entryDetailArea skipTo position
    _ <- updateToolbar(sourceId)
  } yield ()

  private def skipTo(sourceId: Long) = for {
    Some(entryPosition) <- task { entryAccessor firstEntryPositionOf sourceId }
    Some(entry) <- task { entryAccessor findAt entryPosition }
    _ <- entryDetailArea skipTo entryPosition
    _ <- updateToolbar(entry.sourceId)
  } yield ()

  private def fastScrollTo(entryPosition: Int, sourceId: Long) = for {
    _ <- entryDetailArea fastScrollTo entryPosition
    _ <- updateToolbar(sourceId)
  } yield ()

  private def updateToolbar(sourceId: Long) = task {
    rawSourceAccessor.findTitleOf(sourceId) foreach entryDetailArea.updateToolbar
  }
}
