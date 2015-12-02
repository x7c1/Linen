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
  def fastScrollTo(position: Int)(done: OnFinish): Unit = {
    Log info s"[init] position:$position"
    tasks.fastScrollTo(position)(done).execute()
  }
}

import x7c1.wheat.modern.callback.Imports._

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

trait OnSourceSelected {
  def onSourceSelected(event: SourceSelectedEvent):CallbackTask[Unit]
}
trait OnSourceFocused {
  def onSourceFocused(event: SourceFocusedEvent): CallbackTask[Unit]
}
trait OnEntryFocused {
  def onEntryFocused(event: EntryFocusedEvent): CallbackTask[Unit]
}
trait OnEntrySelected {
  def onEntrySelected(event: EntrySelectedEvent): CallbackTask[Unit]
}

case class SourceFocusedEvent(position: Int, source: Source)

case class EntryFocusedEvent(position: Int, entry: Entry)

class ContainerAction(
  container: PaneContainer) extends OnSourceSelected {

  override def onSourceSelected(event: SourceSelectedEvent): CallbackTask[Unit] = {
    Log info "[init]"
    task of container.scrollTo(container.entryArea)
  }
}

class SourceAreaAction(
  container: PaneContainer,
  sourceAccessor: SourceAccessor
) extends OnSourceSelected
  with OnEntrySelected with OnEntryFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    scrollTo(event.position)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    fastScrollTo(event.entry.sourceId)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    fastScrollTo(event.entry.sourceId)
  }
  private def scrollTo(position: Int) = for {
    _ <- task of container.sourceArea.scrollTo(position)
  } yield {}

  private def fastScrollTo(sourceId: Long) = for {
    Some(position) <- task { sourceAccessor positionOf sourceId }
    _ <- task of container.sourceArea.fastScrollTo(position) _
  } yield {}

}

class EntryAreaAction(
  container: PaneContainer)
  extends OnSourceSelected with OnSourceFocused {

  override def onSourceSelected(event: SourceSelectedEvent): CallbackTask[Unit] = {
    displayOrLoad(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent): CallbackTask[Unit] = {
    displayOrLoad(event.source.id)
  }
  private def displayOrLoad(sourceId: Long) = {
    task of container.entryArea.displayOrLoad(sourceId) _
  }
}

class DetailAreaAction(
  container: PaneContainer,
  entryAccessor: EntryAccessor )
  extends OnSourceSelected with OnSourceFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = for {
    Some(entryId) <- task { entryAccessor firstEntryIdOf event.source.id }
    entryPosition <- task { entryAccessor indexOf entryId }
    _ <- task of container.entryDetailArea.fastScrollTo(entryPosition) _
    _ <- task { container.entryDetailArea.updateToolbar(entryId) }
  } yield {
    Log debug s"[ok] sourceId:${event.source.id}, entryId:$entryId"
  }
  override def onSourceFocused(event: SourceFocusedEvent): CallbackTask[Unit] = {
    container.entryDetailArea.updateSource(event.source.id)
  }
}

class PrefetcherAction(
  prefetcher: EntryPrefetcher)
  extends OnSourceSelected
  with OnSourceFocused {

  override def onSourceSelected(event: SourceSelectedEvent): CallbackTask[Unit] = {
    task { prefetcher.triggerBy(event.source.id) }
  }
  override def onSourceFocused(event: SourceFocusedEvent): CallbackTask[Unit] = {
    task { prefetcher.triggerBy(event.source.id) }
  }
}

class Actions (
  val container: ContainerAction,
  val sourceArea: SourceAreaAction,
  val entryArea: EntryAreaAction,
  val detailArea: DetailAreaAction,
  val prefetcher: PrefetcherAction
)
