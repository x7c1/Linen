package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class SourceFocusObserver(
  sourceAccessor: SourceAccessor,
  entryPrefetcher: EntryPrefetcher,
  entryArea: EntryArea ) extends OnSourceFocusedListener {

  override def onSourceFocused(event: SourceFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val source = sourceAccessor get event.position
    val load = for {
      _ <- task of entryArea.displayOrLoad(source.id) _
    } yield {
      Log debug s"[ok] load entries of source-${source.id}"
    }
    val prefetch = for {
      _ <- task apply entryPrefetcher.triggerBy(source.id)
    } yield {
      Log debug s"[ok] prefetch started around sourceId:${source.id}"
    }
    Seq(load, prefetch) foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
