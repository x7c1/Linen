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
    for {
      Some(sourceId) <- task(event.source map (_.id))
      _ <- fastScrollTo(sourceId)
    } yield ()
  }
  override def onSourceSkipStopped(event: SourceSkipStopped) = for {
    Some(sourceId) <- task(event.currentSource map (_.id))
    Some(n) <- findEntryPosition(sourceId)
    _ <- skipTo(n, sourceId)
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
      _ <- outlineArea skipTo event.nextPosition
      Some(sourceId) <- task(event.nextSourceId)
      _ <- updateToolbar(sourceId)
    } yield ()
  }
  override def onDetailSelected(event: DetailSelectedEvent) = {
    fastScrollTo(event.position, event.entry.sourceId)
  }
  override def onDetailFocused(event: DetailFocusedEvent) = {
    for {
      _ <- outlineArea fastScrollTo event.position
      Some(sourceId) <- task(event.sourceId)
      _ <- updateToolbar(sourceId)
    } yield ()
  }
  override def onDetailSkipStopped(event: EntrySkipStopped) = {
    for {
      _ <- outlineArea skipTo event.currentPosition
      Some(sourceId) <- task(event.currentSourceId)
      _ <- updateToolbar(sourceId)
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
