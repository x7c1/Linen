package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.kinds.CallbackTask
import x7c1.wheat.modern.kinds.CallbackTask.taskOf

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task

class SourceSelectObserver(
  container: PaneContainer ) extends OnSourceSelectedListener {

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info event

    val focus = for {
      _ <- taskOf(container.sources scrollTo event.position)
      _ <- taskOf(container scrollTo container.entries)
    } yield {
      Log info s"[done] focus source-${event.sourceId}"
    }
    val show = for {
      _ <- taskOf(container.entries displayOrLoad event.sourceId)
    } yield {
      Log info s"[done] show source-${event.sourceId}"
    }
    Seq(focus, show) foreach runAsync
  }

  def runAsync[A](task: CallbackTask[A]) = {
    Task(task()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
