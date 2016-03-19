package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.unread.{RawSourceAccessor, EntryAccessor, UnreadSourceAccessor}
import x7c1.linen.modern.display.unread.{OutlineSelectedEvent, DetailSelectedEvent, OutlineArea, SourceSelectedEvent}
import x7c1.linen.modern.struct.UnreadOutline
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task

class OutlineAreaAction(
  outlineArea: OutlineArea,
  sourceAccessor: UnreadSourceAccessor,
  rawSourceAccessor: RawSourceAccessor,
  entryAccessor: EntryAccessor[UnreadOutline]
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipStopped
  with OnOutlineSelected with OnOutlineFocused with OnOutlineSkipped
  with OnDetailSelected with OnDetailFocused with OnDetailSkipStopped {

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

  override def onOutlineSelected(event: OutlineSelectedEvent) = {
    for {
      _ <- outlineArea scrollTo event.position
      _ <- updateToolbar(event.entry.sourceId)
    } yield ()
  }
  override def onOutlineFocused(event: OutlineFocusedEvent) = {
    for {
      Some(sourceId) <- task(event.sourceId)
      _ <- updateToolbar(sourceId)
    } yield ()
  }
  override def onOutlineSkipped(event: EntrySkippedEvent) = {
    for {
      Some(sourceId) <- task(event.nextSourceId)
      _ <- skipTo(event.nextPosition, sourceId)
    } yield ()
  }
  override def onDetailSelected(event: DetailSelectedEvent) = {
    fastScrollTo(event.position, event.entry.sourceId)
  }
  override def onDetailFocused(event: DetailFocusedEvent) = {
    for {
      Some(sourceId) <- task(event.sourceId)
      _ <- fastScrollTo(event.position, sourceId)
    } yield ()
  }
  override def onDetailSkipStopped(event: EntrySkipStopped) = {
    for {
      Some(sourceId) <- task(event.currentSourceId)
      _ <- skipTo(event.currentPosition, sourceId)
    } yield ()
  }
  private def fastScrollTo(position: Int, sourceId: Long) = for {
    _ <- outlineArea fastScrollTo position
    _ <- updateToolbar(sourceId)
  } yield ()

  private def skipTo(position: Int, sourceId: Long) = for {
    _ <- outlineArea skipTo position
    _ <- updateToolbar(sourceId)
  } yield ()

  private def fastScrollTo(sourceId: Long): CallbackTask[Unit] = for {
    Some(n) <- findEntryPosition(sourceId)
    _ <- fastScrollTo(n, sourceId)
  } yield ()

  private def updateToolbar(sourceId: Long) = task {
    rawSourceAccessor.findTitleOf(sourceId) foreach outlineArea.updateToolbar
  }
  private def findEntryPosition(sourceId: Long) = task {
    entryAccessor firstEntryPositionOf sourceId
  }

}
