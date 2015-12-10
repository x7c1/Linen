package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.EntryAccessor
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, PaneContainer, SourceSelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.tasks.Async

class EntryDetailAreaAction(
  container: PaneContainer,
  entryAccessor: EntryAccessor
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipped
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected with OnEntryDetailFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    fromSourceArea(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = for {
    _ <- Async.await(300)
    _ <- fromSourceArea(event.source.id)
  } yield()

  override def onSourceSkipped(event: SourceSkippedEvent) = for {
    Some(entryPosition) <- task { entryAccessor firstEntryPositionOf event.nextSource.id }
    _ <- container.entryDetailArea.skipTo(entryPosition)
    Some(entry) <- task { entryAccessor findAt entryPosition }
    _ <- task {
      container.entryDetailArea updateToolbar entry.title
    }
  } yield ()

  override def onEntrySelected(event: EntrySelectedEvent) = {
    scrollAndUpdate(event.position, event.entry.title)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    scrollAndUpdate(event.position, event.entry.title)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = for {
    _ <- task of container.entryDetailArea.scrollTo(event.position) _
    _ <- task { container.entryDetailArea.updateToolbar(event.entry.title) }
  } yield ()

  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = task {
    container.entryDetailArea.updateToolbar(event.entry.title)
  }

  private def fromSourceArea(sourceId: Long) = for {
    Some(entryPosition) <- task { entryAccessor firstEntryPositionOf sourceId }
    Some(entry) <- task { entryAccessor findAt entryPosition }
    _ <- container.entryDetailArea skipTo entryPosition
    _ <- task { container.entryDetailArea updateToolbar entry.title }
  } yield ()

  private def scrollAndUpdate(entryPosition: Int, title: String) = for {
    _ <- task of container.entryDetailArea.fastScrollTo(entryPosition) _
    _ <- task { container.entryDetailArea.updateToolbar(title) }
  } yield ()

}
