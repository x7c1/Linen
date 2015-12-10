package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, PaneContainer, SourceSelectedEvent}
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

class EntryAreaAction(
  container: PaneContainer,
  sourceAccessor: SourceAccessor,
  entryAccessor: EntryAccessor
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipped
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected with OnEntryDetailFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    display(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    display(event.source.id)
  }
  override def onSourceSkipped(event: SourceSkippedEvent) = for {
    Some(n) <- getOrCreatePosition(event.nextSource.id)
    _ <- container.entryArea skipTo n
    _ <- task { updateToolbar(event.nextSource.id) }
  } yield ()

  override def onEntrySelected(event: EntrySelectedEvent) = {
    for {
      _ <- task of container.entryArea.scrollTo(event.position) _
      _ <- task { updateToolbar(event.entry.sourceId) }
    } yield ()
  }
  override def onEntryFocused(event: EntryFocusedEvent) = task {
    updateToolbar(event.entry.sourceId)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = {
    syncDisplay(event.position, event.entry.sourceId)
  }
  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = {
    syncDisplay(event.position, event.entry.sourceId)
  }

  private def syncDisplay(position: Int, sourceId: Long) = for {
    _ <- task of container.entryArea.fastScrollTo(position) _
    _ <- task { updateToolbar(sourceId) }
  } yield ()

  private def display(sourceId: Long) = for {
    Some(n) <- getOrCreatePosition(sourceId)
    _ <- task of container.entryArea.fastScrollTo(n) _
    _ <- task { updateToolbar(sourceId) }
  } yield ()

  private def updateToolbar(sourceId: Long): Unit = {
    sourceAccessor positionOf sourceId flatMap
      sourceAccessor.findAt foreach { source =>
        container.entryArea updateToolbar source.title
      }
  }
  private def getOrCreatePosition(sourceId: Long): CallbackTask[Option[Int]] =
    task { entryAccessor firstEntryPositionOf sourceId }
}
