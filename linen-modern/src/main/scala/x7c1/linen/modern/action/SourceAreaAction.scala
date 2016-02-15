package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.UnreadSourceAccessor
import x7c1.linen.modern.display.unread.{OutlineSelectedEvent, DetailSelectedEvent, SourceSelectedEvent, SourceArea}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.Async.await

class SourceAreaAction(
  sourceArea: SourceArea,
  sourceAccessor: UnreadSourceAccessor
) extends OnSourceSelected with OnSourceSkipped
  with OnOutlineSelected with OnOutlineFocused with OnOutlineSkipStopped
  with OnDetailSelected with OnDetailFocused with OnDetailSkipStopped {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    sourceArea scrollTo event.position
  }
  override def onSourceSkipped(event: SourceSkippedEvent) = {
    sourceArea skipTo event.nextPosition
  }
  override def onOutlineSelected(event: OutlineSelectedEvent) = {
    skipTo(event.entry.sourceId)
  }
  override def onOutlineFocused(event: OutlineFocusedEvent) = for {
    _ <- await(300)
    _ <- skipTo(event.sourceId)
  } yield ()

  override def onOutlineSkipStopped(event: EntrySkipStopped) = for {
    _ <- await(300)
    _ <- skipTo(event.currentSourceId)
  } yield ()

  override def onDetailSelected(event: DetailSelectedEvent) = {
    skipTo(event.entry.sourceId)
  }
  override def onDetailFocused(event: DetailFocusedEvent) = for {
    _ <- await(300)
    _ <- skipTo(event.sourceId)
  } yield()

  override def onDetailSkipStopped(event: EntrySkipStopped) = for {
    _ <- await(300)
    _ <- skipTo(event.currentSourceId)
  } yield ()

  private def skipTo(sourceId: Long) = for {
    Some(position) <- task { sourceAccessor positionOf sourceId }
    _ <- sourceArea skipTo position
  } yield ()
}
