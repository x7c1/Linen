package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.EntryAccessor
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, PaneContainer, SourceSelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class EntryDetailAreaAction(
  container: PaneContainer,
  entryAccessor: EntryAccessor
) extends OnSourceSelected with OnSourceFocused
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    fromSource(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    fromSource(event.source.id)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    scrollAndUpdate(event.entry.entryId, event.position)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    scrollAndUpdate(event.entry.entryId, event.position)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = for {
    _ <- task of container.entryDetailArea.scrollTo(event.position) _
    _ <- task { container.entryDetailArea.updateToolbar(event.entry.entryId) }
  } yield ()

  private def fromSource(sourceId: Long) = for {
    Some(entryId) <- task { entryAccessor firstEntryIdOf sourceId }
    entryPosition <- task { entryAccessor indexOf entryId }
    _ <- scrollAndUpdate(entryId, entryPosition)
  } yield ()

  private def scrollAndUpdate(entryId: Long, entryPosition: Int) = for {
    _ <- task of container.entryDetailArea.fastScrollTo(entryPosition) _
    _ <- task { container.entryDetailArea.updateToolbar(entryId) }
  } yield ()
}
