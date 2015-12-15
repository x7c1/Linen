package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.EntryAccessor
import x7c1.linen.modern.display.{EntryDetailArea, EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.EntryDetail
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.Async.await

class EntryDetailAreaAction(
  entryDetailArea: EntryDetailArea,
  entryAccessor: EntryAccessor[EntryDetail]
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipDone
  with OnEntrySelected with OnEntryFocused with OnEntrySkipDone
  with OnEntryDetailSelected with OnEntryDetailFocused with OnEntryDetailSkipped {

  override def onSourceSelected(event: SourceSelectedEvent) = for {
    _ <- await(300)
    _ <- skipTo(event.source.id)
  } yield ()

  override def onSourceFocused(event: SourceFocusedEvent) = for {
    _ <- await(300)
    _ <- skipTo(event.source.id)
  } yield()

  override def onSourceSkipDone(event: SourceSkipDone) = for {
    _ <- await(300)
    Some(entryPosition) <- task {
      entryAccessor firstEntryPositionOf event.currentSource.id
    }
    Some(entry) <- task {
      entryAccessor findAt entryPosition
    }
    _ <- skipTo(entryPosition, entry.fullTitle)
  } yield ()

  override def onEntrySelected(event: EntrySelectedEvent) = {
    fastScrollTo(event.position, event.entry.shortTitle)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    fastScrollTo(event.position, event.entry.shortTitle)
  }
  override def onEntrySkipDone(event: EntrySkipDone) = {
    skipTo(event.currentPosition, event.currentEntry.shortTitle)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = for {
    _ <- entryDetailArea scrollTo event.position
    _ <- task { entryDetailArea updateToolbar event.entry.fullTitle }
  } yield ()

  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = task {
    entryDetailArea.updateToolbar(event.entry.fullTitle)
  }
  override def onEntryDetailSkipped(event: EntrySkippedEvent) = {
    skipTo(event.nextPosition, event.nextEntry.shortTitle)
  }

  private def skipTo(position: Int, title: String) = for {
    _ <- entryDetailArea skipTo position
    _ <- task { entryDetailArea updateToolbar title }
  } yield ()

  private def skipTo(sourceId: Long) = for {
    Some(entryPosition) <- task { entryAccessor firstEntryPositionOf sourceId }
    Some(entry) <- task { entryAccessor findAt entryPosition }
    _ <- entryDetailArea skipTo entryPosition
    _ <- task { entryDetailArea updateToolbar entry.fullTitle }
  } yield ()

  private def fastScrollTo(entryPosition: Int, title: String) = for {
    _ <- entryDetailArea fastScrollTo entryPosition
    _ <- task { entryDetailArea updateToolbar title }
  } yield ()

}
