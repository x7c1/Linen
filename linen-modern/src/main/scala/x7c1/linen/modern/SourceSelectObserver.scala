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
      _ <- task of container.sourceArea.scrollTo(event.position)
      _ <- task of container.scrollTo(container.entryArea)
    } yield {
      Log debug s"[ok] focus source-${event.sourceId}"
    }
    val tasks = observerTasks.commonTo(event.sourceId) :+ focus
    tasks foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
