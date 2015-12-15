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
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipDone
  with OnEntrySelected with OnEntryFocused with OnEntrySkipped
  with OnEntryDetailSelected with OnEntryDetailFocused with OnEntryDetailSkipDone {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    display(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    display(event.source.id)
  }
  override def onSourceSkipDone(event: SourceSkipDone) = for {
    Some(n) <- findEntryPosition(event.currentSource.id)
    _ <- entryArea skipTo n
    _ <- task { updateToolbar(event.currentSource.id) }
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
  override def onEntrySkipped(event: EntrySkippedEvent) = for {
    _ <- entryArea skipTo event.nextPosition
    _ <- task { updateToolbar(event.nextEntry.sourceId) }
  } yield ()

  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = {
    syncDisplay(event.position, event.entry.sourceId)
  }
  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = {
    syncDisplay(event.position, event.entry.sourceId)
  }
  override def onEntryDetailSkipDone(event: EntrySkipDone) = for {
    _ <- entryArea skipTo event.currentPosition
    _ <- task { updateToolbar(event.currentEntry.sourceId) }
  } yield ()

  private def syncDisplay(position: Int, sourceId: Long) = for {
    _ <- entryArea fastScrollTo position
    _ <- task { updateToolbar(sourceId) }
  } yield ()

  private def display(sourceId: Long) = for {
    Some(n) <- findEntryPosition(sourceId)
    _ <- entryArea fastScrollTo n
    _ <- task { updateToolbar(sourceId) }
  } yield ()

  private def updateToolbar(sourceId: Long): Unit = {
    sourceAccessor positionOf sourceId flatMap
      sourceAccessor.findAt foreach { source =>
        entryArea updateToolbar source.title
      }
  }
  private def findEntryPosition(sourceId: Long): CallbackTask[Option[Int]] =
    task { entryAccessor firstEntryPositionOf sourceId }

}
