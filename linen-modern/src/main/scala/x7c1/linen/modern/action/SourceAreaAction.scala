package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.SourceAccessor
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, SourceArea, SourceSelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.Async.await

class SourceAreaAction(
  sourceArea: SourceArea,
  sourceAccessor: SourceAccessor
) extends OnSourceSelected
  with OnSourceSkipped
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected with OnEntryDetailFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    sourceArea scrollTo event.position
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    skipTo(event.entry.sourceId)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = for {
    _ <- await(300)
    _ <- skipTo(event.entry.sourceId)
  } yield ()

  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = {
    skipTo(event.entry.sourceId)
  }
  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = for {
    _ <- await(300)
    _ <- skipTo(event.entry.sourceId)
  } yield()

  override def onSourceSkipped(event: SourceSkippedEvent) = {
    sourceArea skipTo event.nextPosition
  }
  private def skipTo(sourceId: Long) = for {
    Some(position) <- task { sourceAccessor positionOf sourceId }
    _ <- sourceArea skipTo position
  } yield ()

}
