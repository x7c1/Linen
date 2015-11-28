package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class EntryFocusObserver(
  entryAccessor: EntryAccessor,
  observerTasks: EntryRowObserverTasks) extends  OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val entry = entryAccessor.get(event.position)
    val tasks = observerTasks.commonTo(entry.sourceId, event.position)
    tasks foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
