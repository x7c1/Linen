package x7c1.linen.modern.action

import x7c1.linen.modern.display.{EntryDetailArea, EntryArea, PaneContainer, SourceSelectedEvent, EntrySelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class ContainerAction(
  container: PaneContainer,
  entryArea: EntryArea,
  entryDetailArea: EntryDetailArea )
  extends OnSourceSelected with OnEntrySelected {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    task of container.scrollTo(entryArea)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    task of container.scrollTo(entryDetailArea)
  }
}
