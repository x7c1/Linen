package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryLoader, EntryBuffer, EntryCacher, EntryLoadedEvent, OnEntryLoadedListener, SourceAccessor}
import x7c1.linen.modern.display.{EntrySelectedEvent, PaneContainer, SourceSelectedEvent}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.callback.OnFinish

class EntryAreaAction(
  container: PaneContainer,
  sourceAccessor: SourceAccessor,
  entryBuffer: EntryBuffer,
  entryCacher: EntryCacher,
  onEntryLoaded: OnEntryLoadedListener
) extends OnSourceSelected with OnSourceFocused
  with OnEntrySelected with OnEntryFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    display(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    display(event.source.id)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    for {
      _ <- task of container.entryArea.scrollTo(event.position) _
      _ <- task { updateToolbar(event.entry.sourceId) }
    } yield ()
  }
  override def onEntryFocused(event: EntryFocusedEvent) = task {
    updateToolbar(event.entry.sourceId)
  }

  private def display(sourceId: Long) = for {
    _ <- task of displayOrLoad(sourceId) _
    _ <- task { updateToolbar(sourceId) }
  } yield {
    Log debug s"sourceId:$sourceId"
  }
  private def updateToolbar(sourceId: Long): Unit = {
    sourceAccessor positionOf sourceId map sourceAccessor.get foreach { source =>
      container.entryArea updateToolbar source.title
    }
  }
  private def displayOrLoad(sourceId: Long)(done: OnFinish) = {
    entryBuffer firstEntryIdOf sourceId match {
      case Some(entryId) =>
        val position = entryBuffer indexOf entryId
        container.entryArea.fastScrollTo(position)(done)
      case _ =>
        val onLoad = createOnLoadedListener(done)
        new EntryLoader(entryCacher, onLoad) load sourceId
    }
  }
  private def createOnLoadedListener(done: OnFinish) = {
    onEntryLoaded append OnEntryLoadedListener {
      case EntryLoadedEvent(sourceId, entries) =>
        (for {
          position <- task { calculateEntryPositionOf(sourceId) }
          _ <- task of entryBuffer.insertAll(position, sourceId, entries) _
          _ <- task of container.entryArea.fastScrollTo(position) _
        } yield done.evaluate()).execute()
    }
  }
  private def calculateEntryPositionOf(sourceId: Long): Int = {
    val previousId = sourceAccessor.collectLastFrom(sourceId){
      case source if entryBuffer.has(source.id) =>
        entryBuffer.lastEntryIdOf(source.id)
    }
    entryBuffer positionAfter previousId.flatten
  }
}
