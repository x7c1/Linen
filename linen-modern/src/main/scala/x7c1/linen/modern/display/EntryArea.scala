package x7c1.linen.modern.display

import android.support.v7.widget.Toolbar
import scala.collection.mutable
import x7c1.linen.modern.accessor.{EntryLoadedEvent, EntryLoader, EntryCacher, OnEntryLoadedListener, SourceAccessor, EntryBuffer}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.OnFinish
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.ScrollerTasks
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.callback.Imports._

class EntryArea(
  entries: EntryBuffer,
  sources: SourceAccessor,
  onEntryLoaded: OnEntryLoadedListener,
  toolbar: Toolbar,
  tasks: ScrollerTasks,
  entryCacher: EntryCacher,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val loadingMap = mutable.Map[Long, Boolean]()

  def isLoading(sourceId: Long) = loadingMap.getOrElse(sourceId, false)

  def displayOrLoad(sourceId: Long)(done: OnFinish): Unit = {
    Log info s"[init] sourceId:$sourceId"

    if (isLoading(sourceId)){
      Log warn s"[cancel] (sourceId:$sourceId) already loading"
      return
    }
    loadingMap(sourceId) = true

    def execute(f: => Unit) = entries firstEntryIdOf sourceId match {
      case Some(entryId) =>
        val position = entries indexOf entryId
        tasks.fastScrollTo(position)(OnFinish(f)).execute()
      case _ =>
        val onLoad = createListener(OnFinish(f))
        new EntryLoader(entryCacher, onLoad) load sourceId
    }
    execute {
      loadingMap(sourceId) = false
      updateToolbar(sourceId)

      Log info s"[done] sourceId:$sourceId"
      done.evaluate()
    }
  }

  def updateToolbar(sourceId: Long): Unit = {
    sources positionOf sourceId map sources.get foreach { source =>
      toolbar runUi { _ setTitle source.title }
    }
  }

  def scrollTo(position: Int)(done: OnFinish): Unit = {
    tasks.scrollTo(position)(done).execute()
  }
  private def createListener(done: OnFinish) = {
    onEntryLoaded append OnEntryLoadedListener {
      case EntryLoadedEvent(sourceId, loadedEntries) =>
        (for {
          position <- task apply calculateEntryPositionOf(sourceId)
          _ <- task of entries.insertAll(position, sourceId, loadedEntries) _
          _ <- task of tasks.fastScrollTo(position) _
        } yield done.evaluate()).execute()
    }
  }

  private def calculateEntryPositionOf(sourceId: Long): Int = {
    val previousId = sources.collectLastFrom(sourceId){
      case source if entries.has(source.id) =>
        entries.lastEntryIdOf(source.id)
    }
    entries positionAfter previousId.flatten
  }

}
