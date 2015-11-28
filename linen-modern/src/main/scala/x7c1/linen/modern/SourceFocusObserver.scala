package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class SourceFocusObserver(
  sourceAccessor: SourceAccessor,
  observerTasks: SourceRowObserverTasks) extends OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val source = sourceAccessor get event.position
    val focus = for {
      _ <- observerTasks.displayEntryArea(source.id)
      _ <- observerTasks.updateEntryDetailArea(source.id)
    } yield {
      Log debug s"[ok] focus on source.id${source.id}"
    }
    val tasks = Seq(focus, observerTasks.prefetch(source.id))
    tasks foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
