package x7c1.linen.modern.action

import x7c1.linen.modern.display.{EntrySelectedEvent, PaneContainer, SourceSelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class EntryAreaAction(container: PaneContainer)
  extends OnSourceSelected with OnSourceFocused
  with OnEntrySelected with OnEntryFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    displayOrLoad(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    displayOrLoad(event.source.id)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    for {
      _ <- task of container.entryArea.scrollTo(event.position) _
      _ <- task { container.entryArea.updateToolbar(event.entry.sourceId) }
    } yield ()
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    task { container.entryArea.updateToolbar(event.entry.sourceId) }
  }
  private def displayOrLoad(sourceId: Long) = {
    task of container.entryArea.displayOrLoad(sourceId) _
  }
}
