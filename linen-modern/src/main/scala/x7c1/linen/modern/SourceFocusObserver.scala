package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.kinds.CallbackTask
import x7c1.wheat.modern.kinds.CallbackTask.taskOf

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class SourceFocusObserver(
  sourceAccessor: SourceAccessor,
  entryPrefetcher: EntryPrefetcher,
  entryArea: EntryArea ) extends OnSourceFocusedListener {

  override def onSourceFocused(event: SourceFocusedEvent): Unit = {
    val source = sourceAccessor get event.position
    val load = for {
      _ <- taskOf(entryArea displayOrLoad source.id)
    } yield {
      Log info s"[done] load entries of source-${source.id}"
    }
    val prefetch = entryPrefetcher createTaskOf source.id

    Seq(load, prefetch) foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}


