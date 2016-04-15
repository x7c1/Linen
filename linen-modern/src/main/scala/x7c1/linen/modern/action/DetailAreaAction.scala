package x7c1.linen.modern.action

import x7c1.linen.modern.display.unread.{DetailArea, DetailSelectedEvent, OutlineSelectedEvent, SourceSelectedEvent}
import x7c1.linen.repository.entry.unread.{UnreadDetail, EntryAccessor}
import x7c1.linen.repository.source.unread.{RawSourceAccessor, UnreadSourceAccessor}
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

  override def onSourceSelected(event: SourceSelectedEvent) =
    for {
      entryPosition <- task {
        entryAccessor firstEntryPositionOf event.source.id
      }
      entry <- task { entryPosition flatMap entryAccessor.findAt }
    } yield {
      entryPosition map detailArea.skipTo foreach (_.execute())
      entry map (_.sourceId) foreach updateToolBarNow
    }

  override def onSourceFocused(event: SourceFocusedEvent) = for {
    _ <- await(100)
    entryPosition <- task {
      event.source.map(_.id) flatMap
        entryAccessor.firstEntryPositionOf getOrElse
          entryAccessor.length - 1
    }
  } yield {
    detailArea.skipTo(entryPosition).execute()
    updateToolBarNow(event.source.map(_.id))
  }

  override def onSourceSkipStopped(event: SourceSkipStopped) = for {
    _ <- await(150)
  } yield {
    val entryPosition = event.currentSource map (_.id) flatMap
      entryAccessor.firstEntryPositionOf getOrElse
      (entryAccessor.length - 1)

    detailArea.skipTo(entryPosition).execute()
    event.currentSource map (_.id) map updateToolbar foreach (_.execute())
  }

  override def onOutlineSelected(event: OutlineSelectedEvent) = {
    for {
      _ <- detailArea fastScrollTo event.position
      _ <- updateToolbar(event.entry.sourceId)
    } yield ()
  }
  override def onOutlineFocused(event: OutlineFocusedEvent) = {
    for {
      _ <- detailArea fastScrollTo event.position
    } yield {
      updateToolBarNow(event.sourceId)
    }
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

  private def updateToolBarNow(sourceId: Option[Long]): Unit = {
    sourceId flatMap
      rawSourceAccessor.findTitleOf foreach
        detailArea.updateToolbar
  }
  private def updateToolbar(sourceId: Long) = task {
    rawSourceAccessor.findTitleOf(sourceId) foreach detailArea.updateToolbar
  }
  private def updateToolbarIfExist(sourceId: Option[Long]) = {
    task {
      sourceId map
        updateToolbar foreach (_.execute())
    }
  }
}
