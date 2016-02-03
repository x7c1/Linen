package x7c1.linen.modern.action

import x7c1.linen.modern.display.{EntrySelectedEvent, PaneContainer, PaneDragStoppedEvent, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.recycler.{DragEvent, Next, Previous}

class ContainerAction(container: PaneContainer)
  extends OnSourceSelected with OnEntrySelected {

  import container.{entryArea, entryDetailArea, sourceArea}

  override def onSourceSelected(event: SourceSelectedEvent) = {
    container scrollTo entryArea
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    container scrollTo entryDetailArea
  }
  def onBack(): Boolean = {
    val target = container.findCurrentPane collect {
      case x if x == entryArea => sourceArea
      case x if x == entryDetailArea => entryArea
    } match {
      case Some(p) => Some(p)
      case None => container.findPreviousPane collect {
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
    val pane =
      if (event.rejected) Some(event.from)
      else Option(event.from -> event.direction) collect {
        case (`sourceArea`, Next) => entryArea
        case (`sourceArea`, Previous) => sourceArea
        case (`entryArea`, Next) => entryDetailArea
        case (`entryArea`, Previous) => sourceArea
        case (`entryDetailArea`, Next) => entryDetailArea
        case (`entryDetailArea`, Previous) => entryArea
      }

    pane map container.scrollTo match {
      case Some(callback) => callback.execute()
      case None => Log error s"unknown pane: ${event.from}"
    }
  }
}
