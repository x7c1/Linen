package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.display.{EntryArea, EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.EntryOutline
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task

class EntryAreaAction(
  entryArea: EntryArea,
  sourceAccessor: SourceAccessor,
  entryAccessor: EntryAccessor[EntryOutline]
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
    _ <- entryArea skipTo n
    _ <- task { updateToolbar(event.nextSource.id) }
  } yield ()

  override def onEntrySelected(event: EntrySelectedEvent) = {
    for {
      _ <- entryArea scrollTo event.position
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
    _ <- entryArea fastScrollTo position
    _ <- task { updateToolbar(sourceId) }
  } yield ()

  private def display(sourceId: Long) = for {
    Some(n) <- getOrCreatePosition(sourceId)
    _ <- entryArea fastScrollTo n
    _ <- task { updateToolbar(sourceId) }
  } yield ()

  private def updateToolbar(sourceId: Long): Unit = {
    sourceAccessor positionOf sourceId flatMap
      sourceAccessor.findAt foreach { source =>
        entryArea updateToolbar source.title
      }
  }
  private def getOrCreatePosition(sourceId: Long): CallbackTask[Option[Int]] =
    task { entryAccessor firstEntryPositionOf sourceId }
}
