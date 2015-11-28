package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class SourceSelectObserver(
  container: PaneContainer,
  observerTasks: SourceRowObserverTasks ) extends OnSourceSelectedListener {

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val focus = for {
      _ <- observerTasks displayEntryArea event.sourceId
      _ <- task of container.sourceArea.scrollTo(event.position)
      _ <- task of container.scrollTo(container.entryArea)
      _ <- observerTasks updateEntryDetailArea event.sourceId
    } yield {
      Log debug s"[ok] select source-${event.sourceId}"
    }
    val tasks = Seq(focus, observerTasks.prefetch(event.sourceId))
    tasks foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }

}
