package x7c1.linen.modern.action

import x7c1.linen.modern.display.unread.{OutlineSelectedEvent, PaneContainer, PaneDragStoppedEvent, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.observer.recycler.{DragEvent, Next, Previous}

class ContainerAction(container: PaneContainer)
  extends OnSourceSelected with OnOutlineSelected {

  import container.{outlineArea, detailArea, sourceArea}

  override def onSourceSelected(event: SourceSelectedEvent) = {
    container scrollTo outlineArea
  }
  override def onOutlineSelected(event: OutlineSelectedEvent) = {
    container scrollTo detailArea
  }
  def onBack(): Boolean = {
    val target = container.findCurrentPane collect {
      case x if x == outlineArea => sourceArea
      case x if x == detailArea => outlineArea
    } match {
      case Some(p) => Some(p)
      case None => container.findPreviousPane collect {
        case x if x == outlineArea => sourceArea
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
        case (`sourceArea`, Next) => outlineArea
        case (`sourceArea`, Previous) => sourceArea
        case (`outlineArea`, Next) => detailArea
        case (`outlineArea`, Previous) => sourceArea
        case (`detailArea`, Next) => detailArea
        case (`detailArea`, Previous) => outlineArea
      }

    pane map container.scrollTo match {
      case Some(callback) => callback.execute()
      case None => Log error s"unknown pane: ${event.from}"
    }
  }
}
