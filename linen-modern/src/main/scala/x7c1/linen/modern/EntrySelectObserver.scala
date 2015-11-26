package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.Imports._

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class EntrySelectObserver(
  container: PaneContainer) extends OnEntrySelectedListener {

  override def onEntrySelected(event: EntrySelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val scrollEntry = for {
      _ <- task of container.entryArea.scrollTo(event.position) _
    } yield {
      Log info s"[ok] entry scrolled to position:${event.position}"
    }
    val scrollSource = for {
      _ <- task of container.sourceArea.display(event.sourceId)
    } yield {
      Log info s"[ok] source scrolled to sourceId:${event.sourceId}"
    }
    val updateToolbar = for {
      _ <- task apply container.entryArea.updateToolbar(event.sourceId)
    } yield {
      Log info s"[ok] update Toolbar"
    }
    Seq(scrollEntry, scrollSource, updateToolbar) foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
