package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.SourceAccessor
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, PaneContainer, SourceSelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class SourceAreaAction(
  container: PaneContainer,
  sourceAccessor: SourceAccessor
) extends OnSourceSelected
  with OnSourceSkipped
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected with OnEntryDetailFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    task of container.sourceArea.scrollTo(event.position) _
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    fastScrollTo(event.entry.sourceId)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    fastScrollTo(event.entry.sourceId)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = {
    fastScrollTo(event.entry.sourceId)
  }
  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = {
    fastScrollTo(event.entry.sourceId)
  }
  override def onSourceSkipped(event: SourceSkippedEvent) = {
    container.sourceArea.skipTo(event.nextPosition)
  }
  private def fastScrollTo(sourceId: Long) = for {
    Some(position) <- task { sourceAccessor positionOf sourceId }
    _ <- task of container.sourceArea.fastScrollTo(position) _
  } yield {}
}
