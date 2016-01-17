package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryAccessor, UnreadSourceAccessor}
import x7c1.linen.modern.display.{EntryArea, EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.EntryOutline
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task

class EntryAreaAction(
  entryArea: EntryArea,
  sourceAccessor: UnreadSourceAccessor,
  entryAccessor: EntryAccessor[EntryOutline]
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipStopped
  with OnEntrySelected with OnEntryFocused with OnEntrySkipped
  with OnEntryDetailSelected with OnEntryDetailFocused with OnEntryDetailSkipStopped {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    fastScrollTo(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    fastScrollTo(event.source.id)
  }
  override def onSourceSkipStopped(event: SourceSkipStopped) = for {
    Some(n) <- findEntryPosition(event.currentSource.id)
    _ <- skipTo(n, event.currentSource.id)
  } yield ()

  override def onEntrySelected(event: EntrySelectedEvent) = {
    for {
      _ <- entryArea scrollTo event.position
      _ <- updateToolbar(event.entry.sourceId)
    } yield ()
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    updateToolbar(event.entry.sourceId)
  }
  override def onEntrySkipped(event: EntrySkippedEvent) = {
    skipTo(event.nextPosition, event.nextEntry.sourceId)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = {
    fastScrollTo(event.position, event.entry.sourceId)
  }
  override def onEntryDetailFocused(event: EntryDetailFocusedEvent) = {
    fastScrollTo(event.position, event.entry.sourceId)
  }
  override def onEntryDetailSkipStopped(event: EntrySkipStopped) = {
    skipTo(event.currentPosition, event.currentEntry.sourceId)
  }
  private def fastScrollTo(position: Int, sourceId: Long) = for {
    _ <- entryArea fastScrollTo position
    _ <- updateToolbar(sourceId)
  } yield ()

  private def skipTo(position: Int, sourceId: Long) = for {
    _ <- entryArea skipTo position
    _ <- updateToolbar(sourceId)
  } yield ()

  private def fastScrollTo(sourceId: Long): CallbackTask[Unit] = for {
    Some(n) <- findEntryPosition(sourceId)
    _ <- fastScrollTo(n, sourceId)
  } yield ()

  private def updateToolbar(sourceId: Long) = task {
    sourceAccessor positionOf sourceId flatMap
      sourceAccessor.findAt foreach { source =>
        entryArea updateToolbar source.title
      }
  }
  private def findEntryPosition(sourceId: Long) = task {
    entryAccessor firstEntryPositionOf sourceId
  }

}
