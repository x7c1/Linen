package x7c1.linen.modern

import android.support.v7.widget.{RecyclerView, Toolbar}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.callback.Imports._
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.observer.{FocusedEventFactory, ItemFocusedEvent}
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

trait OnSourceSelected {
  def onSourceSelected(event: SourceSelectedEvent): CallbackTask[Unit]
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
trait OnEntryDetailSelected {
  def onEntryDetailSelected(event: EntryDetailSelectedEvent): CallbackTask[Unit]
}

case class SourceFocusedEvent(
  override val position: Int,
  source: Source) extends ItemFocusedEvent

class SourceFocusedEventFactory(sourceAccessor: SourceAccessor)
  extends FocusedEventFactory[SourceFocusedEvent] {

  override def createAt(position: Int) = {
    val source = sourceAccessor get position
    SourceFocusedEvent(position, source)
  }
}

case class EntryFocusedEvent(
  override val position: Int,
  entry: Entry) extends ItemFocusedEvent

class EntryFocusedEventFactory(entryAccessor: EntryAccessor)
  extends FocusedEventFactory[EntryFocusedEvent] {

  override def createAt(position: Int) = {
    val entry = entryAccessor get position
    EntryFocusedEvent(position, entry)
  }
}

class ContainerAction(container: PaneContainer)
  extends OnSourceSelected with OnEntrySelected {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    task of container.scrollTo(container.entryArea)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    task of container.scrollTo(container.entryDetailArea)
  }
}

class SourceAreaAction(
  container: PaneContainer,
  sourceAccessor: SourceAccessor
) extends OnSourceSelected
  with OnEntrySelected with OnEntryFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    task of container.sourceArea.scrollTo(event.position)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    fastScrollTo(event.entry.sourceId)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    fastScrollTo(event.entry.sourceId)
  }
  private def fastScrollTo(sourceId: Long) = for {
    Some(position) <- task { sourceAccessor positionOf sourceId }
    _ <- task of container.sourceArea.fastScrollTo(position) _
  } yield {}

}

class EntryAreaAction(container: PaneContainer)
  extends OnSourceSelected with OnSourceFocused
  with OnEntrySelected with OnEntryFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    displayOrLoad(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    displayOrLoad(event.source.id)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    for {
      _ <- task of container.entryArea.scrollTo(event.position) _
      _ <- task { container.entryArea.updateToolbar(event.entry.sourceId) }
    } yield ()
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    task { container.entryArea.updateToolbar(event.entry.sourceId) }
  }
  private def displayOrLoad(sourceId: Long) = {
    task of container.entryArea.displayOrLoad(sourceId) _
  }
}

class EntryDetailAreaAction(
  container: PaneContainer,
  entryAccessor: EntryAccessor
) extends OnSourceSelected with OnSourceFocused
  with OnEntrySelected with OnEntryFocused
  with OnEntryDetailSelected {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    fromSource(event.source.id)
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    fromSource(event.source.id)
  }
  override def onEntrySelected(event: EntrySelectedEvent) = {
    scrollAndUpdate(event.entry.entryId, event.position)
  }
  override def onEntryFocused(event: EntryFocusedEvent) = {
    scrollAndUpdate(event.entry.entryId, event.position)
  }
  override def onEntryDetailSelected(event: EntryDetailSelectedEvent) = for {
    _ <- task of container.entryDetailArea.scrollTo(event.position) _
    _ <- task { container.entryDetailArea.updateToolbar(event.entry.entryId) }
  } yield ()

  private def fromSource(sourceId: Long) = for {
    Some(entryId) <- task { entryAccessor firstEntryIdOf sourceId }
    entryPosition <- task { entryAccessor indexOf entryId }
    _ <- scrollAndUpdate(entryId, entryPosition)
  } yield ()

  private def scrollAndUpdate(entryId: Long, entryPosition: Int) = for {
    _ <- task of container.entryDetailArea.fastScrollTo(entryPosition) _
    _ <- task { container.entryDetailArea.updateToolbar(entryId) }
  } yield ()
}

class PrefetcherAction(
  prefetcher: EntryPrefetcher
) extends OnSourceSelected with OnSourceFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    task { prefetcher.triggerBy(event.source.id) }
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    task { prefetcher.triggerBy(event.source.id) }
  }
}

class Actions (
  val container: ContainerAction,
  val sourceArea: SourceAreaAction,
  val entryArea: EntryAreaAction,
  val detailArea: EntryDetailAreaAction,
  val prefetcher: PrefetcherAction
)
