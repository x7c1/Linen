package x7c1.linen.modern

import android.support.v7.widget.{RecyclerView, Toolbar}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.tasks.ScrollerTasks

import scala.collection.mutable

trait Pane {
  def displayPosition: Int
}

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

  def updateToolbar(sourceId: Long) = {
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

class SourceArea(
  sources: SourceAccessor,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val tasks = ScrollerTasks(recyclerView, 125F)

  def display(sourceId: Long)(done: OnFinish): CallbackTask[Unit] =
    for {
      Some(position) <- task(sources positionOf sourceId)
      _ <- tasks.fastScrollTo(position)(done)
    } yield ()

  def scrollTo(position: Int): OnFinish => Unit = done => {
    Log info s"[init] position:$position"
    tasks.scrollTo(position)(done).execute()
  }
}

import x7c1.wheat.modern.callback.Imports._

class SourceRowObserverTasks(
  container: PaneContainer,
  entries: EntryAccessor,
  prefetcher: EntryPrefetcher ) {

  def displayEntryArea(sourceId: Long): CallbackTask[Unit] = for {
    _ <- task of container.entryArea.displayOrLoad(sourceId) _
  } yield {
    Log debug s"[ok] sourceId:$sourceId"
  }

  def updateEntryDetailArea(sourceId: Long): CallbackTask[Unit] = for {
    Some(entryId) <- task(entries.firstEntryIdOf(sourceId))
    _ <- task of container.entryDetailArea.fastScrollTo(entries indexOf entryId) _
    _ <- task { container.entryDetailArea.updateToolbar(entryId)}
  } yield {
    Log debug s"[ok] sourceId:$sourceId, entryId:$entryId"
  }
  def prefetch(sourceId: Long): CallbackTask[Unit] = for {
    _ <- task apply prefetcher.triggerBy(sourceId)
  } yield {
    Log debug s"[ok] prefetch started around sourceId:$sourceId"
  }
}

class EntryRowObserverTasks (
  container: PaneContainer,
  entries: EntryAccessor ){

  def commonTo(sourceId: Long, entryPosition: Int): Seq[CallbackTask[Unit]] =
    Seq(
      scrollSourceArea(sourceId),
      updateEntryAreaToolbar(sourceId),
      scrollEntryDetailArea(entryPosition)
    )

  private def scrollSourceArea(sourceId: Long) = for {
    _ <- task of container.sourceArea.display(sourceId) _
  } yield {
    Log debug s"[ok] sourceId:$sourceId"
  }
  private def scrollEntryDetailArea(position: Int) = for {
    _ <- task of container.entryDetailArea.fastScrollTo(position) _
    entryId <- task(entries.get(position).entryId)
    _ <- task { container.entryDetailArea.updateToolbar(entryId)}
  } yield {
    Log debug s"[ok] position:$position"
  }
  private def updateEntryAreaToolbar(sourceId: Long) = for {
    _ <- task apply container.entryArea.updateToolbar(sourceId)
  } yield {
    Log debug s"[ok] sourceId:$sourceId"
  }
}
