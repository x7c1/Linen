package x7c1.linen.modern.action

import x7c1.linen.modern.display.{EntryArea, EntryDetailArea, EntrySelectedEvent, PaneContainer, PaneFlungEvent, SourceArea, SourceSelectedEvent}

class ContainerAction(
  container: PaneContainer,
  sourceArea: SourceArea,
  entryArea: EntryArea,
  entryDetailArea: EntryDetailArea )
  extends OnPaneFlung with OnSourceSelected with OnEntrySelected {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    container scrollTo entryArea
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    container scrollTo entryDetailArea
  }
  override def onPaneFlung(event: PaneFlungEvent): Boolean = {
    container.scrollBy((1.2 * event.distanceX).toInt)
    true
  }
}
