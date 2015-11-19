package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.kinds.CallbackTask.taskOf

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

class SourceFocusObserver(
  sourceAccessor: SourceAccessor,
  entryArea: EntryArea ) extends OnSourceFocusedListener {

  override def onSourceFocused(event: SourceFocusedEvent): Unit = {
    val source = sourceAccessor get event.position
    val load = for {
      _ <- taskOf(entryArea displayOrLoad source.id)
    } yield {
      Log info s"[done] load entries of source-${source.id}"
    }
    Task(load()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }
}
