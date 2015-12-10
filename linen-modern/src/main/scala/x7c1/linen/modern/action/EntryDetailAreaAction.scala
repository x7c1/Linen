package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.EntryAccessor
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, PaneContainer, SourceSelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class EntryDetailAreaAction(
  container: PaneContainer,
  entryAccessor: EntryAccessor
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipped
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected with OnEntryDetailFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    fromSource(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    fromSource(event.source.id)
  }
  override def onSourceSkipped(event: SourceSkippedEvent) = for {
    Some(entryPosition) <- task { entryAccessor firstEntryPositionOf event.nextSource.id }
    _ <- container.entryDetailArea.skipTo(entryPosition)
    _ <- task { container.entryDetailArea.updateToolbar(entryPosition) }
  } yield ()

  override def onEntrySelected(event: EntrySelectedEvent) = {
    scrollAndUpdate(event.position)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    scrollAndUpdate(event.position)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = for {
    _ <- task of container.entryDetailArea.scrollTo(event.position) _
    _ <- task { container.entryDetailArea.updateToolbar(event.position) }
  } yield ()

  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = task {
    container.entryDetailArea.updateToolbar(event.position)
  }

  private def fromSource(sourceId: Long) = for {
    Some(entryPosition) <- task { entryAccessor firstEntryPositionOf sourceId }
    _ <- scrollAndUpdate(entryPosition)
  } yield ()

  private def scrollAndUpdate(entryPosition: Int) = for {
    _ <- task of container.entryDetailArea.fastScrollTo(entryPosition) _
    _ <- task { container.entryDetailArea.updateToolbar(entryPosition) }
  } yield ()

}
