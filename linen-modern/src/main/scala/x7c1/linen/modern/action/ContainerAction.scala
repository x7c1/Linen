package x7c1.linen.modern.action

import x7c1.linen.modern.display.{PaneDragStoppedEvent, EntryArea, EntryDetailArea, EntrySelectedEvent, PaneContainer, SourceArea, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.recycler.{DragEvent, Previous, Next}

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
  def onPaneDragging(event: DragEvent): Unit = {
    container.scrollBy(- event.distance.toInt)
  }
  def onPaneDragStopped(event: PaneDragStoppedEvent): Unit = {
    Log info s"$event"

    event.direction match {
      case Next =>
        container.scrollTo(entryArea).execute()
      case Previous =>
        container.scrollTo(sourceArea).execute()
    }
  }
}
