package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.display.{EntrySelectedEvent, PaneContainer, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class EntryAreaAction(
  container: PaneContainer,
  sourceAccessor: SourceAccessor,
  entryAccessor: EntryAccessor,
  entryBufferUpdater: EntryBufferUpdater
) extends OnSourceSelected with OnSourceFocused
  with OnEntrySelected with OnEntryFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    display(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    display(event.source.id)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    for {
      _ <- task of container.entryArea.scrollTo(event.position) _
      _ <- task { updateToolbar(event.entry.sourceId) }
    } yield ()
  }
  override def onEntryFocused(event: EntryFocusedEvent) = task {
    updateToolbar(event.entry.sourceId)
  }

  private def display(sourceId: Long) = for {
    n <- getOrCreatePosition(sourceId)
    _ <- task of container.entryArea.fastScrollTo(n) _
    _ <- task { updateToolbar(sourceId) }
  } yield {
    Log debug s"sourceId:$sourceId"
  }
  private def updateToolbar(sourceId: Long): Unit = {
    sourceAccessor positionOf sourceId map sourceAccessor.get foreach { source =>
      container.entryArea updateToolbar source.title
    }
  }
  private def getOrCreatePosition(sourceId: Long) =
    entryAccessor firstEntryIdOf sourceId match {
      case Some(entryId) =>
        task { entryAccessor indexOf entryId }
      case _ =>
        for { event <- task of entryBufferUpdater.loadAndInsert(sourceId) _ }
        yield event.position
    }

}
