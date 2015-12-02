package x7c1.linen.modern.action

import x7c1.linen.modern.display.{PaneContainer, SourceSelectedEvent, EntrySelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class ContainerAction(container: PaneContainer)
  extends OnSourceSelected with OnEntrySelected {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    task of container.scrollTo(container.entryArea)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    task of container.scrollTo(container.entryDetailArea)
  }
}
