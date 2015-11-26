package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task

class EntryFocusObserver(
  entryAccessor: EntryAccessor,
  sourceArea: SourceArea) extends OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val scrollSource = for {
      entry <- task apply entryAccessor.get(event.position)
      _ <- task of sourceArea.display(entry.sourceId)
    } yield {
      Log info s"[ok] source scrolled to sourceId:${entry.sourceId}"
    }
    Seq(scrollSource) foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
