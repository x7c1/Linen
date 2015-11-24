package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.kinds.CallbackTask
import x7c1.wheat.modern.kinds.CallbackTask.task
import x7c1.wheat.modern.kinds.callback.Imports._

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class SourceSelectObserver(
  container: PaneContainer,
  entryPrefetcher: EntryPrefetcher ) extends OnSourceSelectedListener {

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val focus = for {
      _ <- task of container.sourceArea.scrollTo(event.position)
      _ <- task of container.scrollTo(container.entryArea)
    } yield {
      Log debug s"[ok] focus source-${event.sourceId}"
    }
    val show = for {
      _ <- task of container.entryArea.displayOrLoad(event.sourceId) _
    } yield {
      Log debug s"[ok] show source-${event.sourceId}"
    }
    val prefetch = for {
      _ <- task apply entryPrefetcher.triggerBy(event.sourceId)
    } yield {
      Log debug s"[ok] prefetch started around sourceId:${event.sourceId}"
    }
    Seq(focus, show, prefetch) foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
