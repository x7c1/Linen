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
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected with OnEntryDetailFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = for {
    _ <- await(0)
    _ <- fromSourceArea(event.source.id)
  } yield ()

  override def onSourceFocused(event: SourceFocusedEvent) = for {
    _ <- await(300)
    _ <- fromSourceArea(event.source.id)
  } yield()

  override def onSourceSkipDone(event: SourceSkipDone) = for {
    _ <- await(300)
    Some(entryPosition) <- task {
      entryAccessor firstEntryPositionOf event.currentSource.id
    }
    _ <- entryDetailArea skipTo entryPosition
    Some(entry) <- task {
      entryAccessor findAt entryPosition
    }
    _ <- task {
      entryDetailArea updateToolbar entry.fullTitle
    }
  } yield ()

  override def onEntrySelected(event: EntrySelectedEvent) = {
    scrollAndUpdate(event.position, event.entry.shortTitle)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    scrollAndUpdate(event.position, event.entry.shortTitle)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = for {
    _ <- entryDetailArea scrollTo event.position
    _ <- task { entryDetailArea updateToolbar event.entry.fullTitle }
  } yield ()

  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = task {
    entryDetailArea.updateToolbar(event.entry.fullTitle)
  }

  private def fromSourceArea(sourceId: Long) = for {
    Some(entryPosition) <- task { entryAccessor firstEntryPositionOf sourceId }
    Some(entry) <- task { entryAccessor findAt entryPosition }
    _ <- entryDetailArea skipTo entryPosition
    _ <- task { entryDetailArea updateToolbar entry.fullTitle }
  } yield ()

  private def scrollAndUpdate(entryPosition: Int, title: String) = for {
    _ <- entryDetailArea fastScrollTo entryPosition
    _ <- task { entryDetailArea updateToolbar title }
  } yield ()

}
