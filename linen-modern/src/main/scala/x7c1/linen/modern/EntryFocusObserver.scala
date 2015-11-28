package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class EntryFocusObserver(
  entryAccessor: EntryAccessor,
  entryArea: EntryArea,
  sourceArea: SourceArea) extends OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val entry = entryAccessor.get(event.position)

    val scrollSource = for {
      _ <- task of sourceArea.display(entry.sourceId) _
    } yield {
      Log info s"[ok] source scrolled to sourceId:${entry.sourceId}"
    }
    val updateToolbar = for {
      _ <- task apply entryArea.updateToolbar(entry.sourceId)
    } yield {
      Log info s"[ok] update Toolbar"
    }
    Seq(scrollSource, updateToolbar) foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
