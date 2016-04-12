package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.unread.RawSourceAccessor
import x7c1.linen.modern.display.unread.{DetailSelectedEvent, OutlineArea, OutlineSelectedEvent, SourceSelectedEvent}
import x7c1.linen.repository.entry.unread.{UnreadOutline, EntryAccessor}
import x7c1.linen.repository.source.unread.UnreadSourceAccessor
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
    task {
      val sourceId = event.source.id
      entryAccessor firstEntryPositionOf sourceId map
        outlineArea.fastScrollTo foreach (_.execute())

      updateToolbar(sourceId).execute()
    }
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    task {
      val position = event.source map (_.id) flatMap
        entryAccessor.firstEntryPositionOf getOrElse
          (entryAccessor.length - 1)

      outlineArea.fastScrollTo(position).execute()
      event.source map (_.id) map updateToolbar foreach (_.execute())
    }
  }
  override def onSourceSkipStopped(event: SourceSkipStopped) = {
    task {
      val entryPosition = event.currentSource map (_.id) flatMap
        entryAccessor.firstEntryPositionOf getOrElse
          (entryAccessor.length - 1)

      outlineArea.skipTo(entryPosition).execute()
      event.currentSource map (_.id) map updateToolbar foreach (_.execute())
    }
  }

  override def onOutlineSelected(event: OutlineSelectedEvent) = {
    for {
      _ <- outlineArea scrollTo event.position
      _ <- updateToolbar(event.entry.sourceId)
    } yield ()
  }
  override def onOutlineFocused(event: OutlineFocusedEvent) = {
    task {
      event.sourceId map
        updateToolbar foreach (_.execute())
    }
  }
  override def onOutlineSkipped(event: EntrySkippedEvent) = {
    for {
      _ <- outlineArea skipTo event.nextPosition
    } yield {
      event.nextSourceId map
        updateToolbar foreach (_.execute())
    }
  }
  override def onDetailSelected(event: DetailSelectedEvent) = {
    fastScrollTo(event.position, event.entry.sourceId)
  }
  override def onDetailFocused(event: DetailFocusedEvent) = {
    for {
      _ <- outlineArea fastScrollTo event.position
    } yield {
      event.sourceId map updateToolbar foreach (_.execute())
    }
  }
  override def onDetailSkipStopped(event: EntrySkipStopped) = {
    for {
      _ <- outlineArea skipTo event.currentPosition
    } yield {
      event.currentSourceId map updateToolbar foreach (_.execute())
    }
  }
  private def fastScrollTo(position: Int, sourceId: Long) = for {
    _ <- outlineArea fastScrollTo position
    _ <- updateToolbar(sourceId)
  } yield ()

  private def updateToolbar(sourceId: Long) = task {
    rawSourceAccessor.findTitleOf(sourceId) foreach outlineArea.updateToolbar
  }

}
