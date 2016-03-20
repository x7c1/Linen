package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.unread.{EntryAccessor, RawSourceAccessor, UnreadSourceAccessor}
import x7c1.linen.modern.display.unread.{DetailArea, DetailSelectedEvent, OutlineSelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.UnreadDetail
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.Async.await

class DetailAreaAction(
  detailArea: DetailArea,
  sourceAccessor: UnreadSourceAccessor,
  rawSourceAccessor: RawSourceAccessor,
  entryAccessor: EntryAccessor[UnreadDetail]
) extends OnSourceSelected with OnSourceFocused with OnSourceSkipStopped
  with OnOutlineSelected with OnOutlineFocused with OnOutlineSkipStopped
  with OnDetailSelected with OnDetailFocused with OnDetailSkipped {

  override def onSourceSelected(event: SourceSelectedEvent) = for {
    _ <- skipTo(event.source.id)
  } yield ()

  override def onSourceFocused(event: SourceFocusedEvent) = for {
    _ <- await(100)
    _ <- skipTo(event.source.id)
  } yield()

  override def onSourceSkipStopped(event: SourceSkipStopped) = for {
    _ <- await(150)
    Some(entryPosition) <- task {
      entryAccessor firstEntryPositionOf event.currentSource.id
    }
    Some(entry) <- task {
      entryAccessor findAt entryPosition
    }
    _ <- skipTo(entryPosition, entry.sourceId)
  } yield ()

  override def onOutlineSelected(event: OutlineSelectedEvent) = {
    fastScrollTo(event.position, event.entry.sourceId)
  }
  override def onOutlineFocused(event: OutlineFocusedEvent) = {
    for {
      _ <- detailArea fastScrollTo event.position
      Some(sourceId) <- task(event.sourceId)
      _ <- updateToolbar(sourceId)
    } yield ()
  }
  override def onOutlineSkipStopped(event: EntrySkipStopped) = {
    skipTo(event.currentPosition, event.currentSourceId)
  }
  override def onDetailSelected(event: DetailSelectedEvent) = for {
    _ <- detailArea scrollTo event.position
    _ <- updateToolbar(event.entry.sourceId)
  } yield ()

  override def onDetailFocused(event: DetailFocusedEvent) =
    updateToolbarIfExist(event.sourceId)

  override def onDetailSkipped(event: EntrySkippedEvent) = {
    skipTo(event.nextPosition, event.nextSourceId)
  }

  private def skipTo(position: Int, sourceId: Option[Long]) = {
    for {
      _ <- detailArea skipTo position
      _ <- updateToolbarIfExist(sourceId)
    } yield ()
  }
  private def skipTo(sourceId: Long) = for {
    Some(entryPosition) <- task { entryAccessor firstEntryPositionOf sourceId }
    Some(entry) <- task { entryAccessor findAt entryPosition }
    _ <- detailArea skipTo entryPosition
    _ <- updateToolbarIfExist(entry.sourceId)
  } yield ()

  private def fastScrollTo(entryPosition: Int, sourceId: Long) = for {
    _ <- detailArea fastScrollTo entryPosition
    _ <- updateToolbar(sourceId)
  } yield ()

  private def updateToolbar(sourceId: Long) = task {
    rawSourceAccessor.findTitleOf(sourceId) foreach detailArea.updateToolbar
  }
  private def updateToolbarIfExist(sourceId: Option[Long]) = {
    for {
      Some(id) <- task(sourceId)
      _ <- updateToolbar(id)
    } yield ()
  }
}
