package x7c1.linen.modern.action

import x7c1.linen.modern.display.{EntryArea, EntryDetailArea, EntrySelectedEvent, PaneContainer, PaneDragStoppedEvent, SourceArea, SourceSelectedEvent}
import x7c1.wheat.modern.observer.recycler.{DragEvent, Next, Previous}

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
  def onBack(): Boolean = {
    val panes = Seq(
      entryDetailArea,
      entryArea,
      sourceArea
    )
    val target = container findCurrentPane panes collect {
      case x if x == entryArea => sourceArea
      case x if x == entryDetailArea => entryArea
    } match {
      case Some(p) => Some(p)
      case None => container findPreviousPane panes collect {
        case x if x == entryArea => sourceArea
      }
    }
    target match {
      case Some(pane) =>
        container.scrollTo(pane).execute()
        true
      case None =>
        false
    }
  }
  def onPaneDragging(event: DragEvent): Unit = {
    container.scrollBy(- event.distance.toInt)
  }
  def onPaneDragStopped(event: PaneDragStoppedEvent): Unit = {
    import x7c1.linen.modern.display.PaneLabel._
    val pane =
      if (event.rejected) event.from match {
        case SourceArea => sourceArea
        case EntryArea => entryArea
        case EntryDetailArea => entryDetailArea
      } else (event.from, event.direction) match {
        case (SourceArea, Next) => entryArea
        case (SourceArea, Previous) => sourceArea
        case (EntryArea, Next) => entryDetailArea
        case (EntryArea, Previous) => sourceArea
        case (EntryDetailArea, Next) => entryDetailArea
        case (EntryDetailArea, Previous) => entryArea
      }

    container.scrollTo(pane).execute()
  }
}
