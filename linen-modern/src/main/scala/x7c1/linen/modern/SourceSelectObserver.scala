package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.kinds.CallbackTask
import x7c1.wheat.modern.kinds.CallbackTask.taskOf

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task

class SourceSelectObserver(
  container: PaneContainer,
  entryPrefetcher: EntryPrefetcher ) extends OnSourceSelectedListener {

  override def onSourceSelected(event: SourceSelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val focus = for {
      _ <- taskOf(container.sourceArea scrollTo event.position)
      _ <- taskOf(container scrollTo container.entryArea)
    } yield {
      Log debug s"[ok] focus source-${event.sourceId}"
    }
    val show = for {
      _ <- taskOf(container.entryArea displayOrLoad event.sourceId)
    } yield {
      Log debug s"[ok] show source-${event.sourceId}"
    }
    val prefetch = for {
      _ <- taskOf(entryPrefetcher triggerBy event.sourceId)
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
