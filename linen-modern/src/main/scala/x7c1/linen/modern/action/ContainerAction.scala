package x7c1.linen.modern.action

import x7c1.linen.modern.display.{EntryArea, EntryDetailArea, EntrySelectedEvent, PaneContainer, SourceSelectedEvent}

class ContainerAction(
  container: PaneContainer,
  entryArea: EntryArea,
  entryDetailArea: EntryDetailArea )
  extends OnSourceSelected with OnEntrySelected {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    container scrollTo entryArea
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    container scrollTo entryDetailArea
  }
}
