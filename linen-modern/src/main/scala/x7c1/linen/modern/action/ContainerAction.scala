package x7c1.linen.modern.action

import x7c1.linen.modern.display.{PaneDragStoppedEvent, EntryArea, EntryDetailArea, EntrySelectedEvent, PaneContainer, PaneDragEvent, SourceArea, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log

class ContainerAction(
  container: PaneContainer,
  sourceArea: SourceArea,
  entryArea: EntryArea,
  entryDetailArea: EntryDetailArea )
  extends OnSourceSelected with OnEntrySelected {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    container scrollTo entryArea
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    container scrollTo entryDetailArea
  }
  def onPaneDragging(event: PaneDragEvent): Unit = {
    container.scrollBy(event.distanceX.toInt)
  }
  def onPaneDragStopped(event: PaneDragStoppedEvent): Unit = {
    Log info s"$event"

    if (event.direction < 0)
      container.scrollTo(entryArea).execute()
    else
      container.scrollTo(sourceArea).execute()
  }
}
