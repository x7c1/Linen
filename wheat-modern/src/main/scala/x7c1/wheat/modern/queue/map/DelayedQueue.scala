package x7c1.wheat.modern.queue.map

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.fate.FateProvider.{ErrorLike, HasContext}
import x7c1.wheat.modern.fate.FutureFate
import x7c1.wheat.modern.fate.FutureFate.HasTimer
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.kinds.Fate

import scala.concurrent.duration.DurationInt

trait DelayedQueue[CONTEXT, ERROR, X] {

  def enqueue(value: X): Fate[CONTEXT, ERROR, Unit]
}

private object DelayedQueue {
  def apply[C: HasContext : HasTimer, E: ErrorLike, X, Y](
    createQueue: () => GroupingQueue[X],
    callee: X => Y,
    onDequeue: (X, Either[E, Y]) => Unit ): DelayedQueue[C, E, X] = {

    new DelayedQueueImpl(createQueue, callee, onDequeue)
  }
}

private class DelayedQueueImpl[C: HasContext : HasTimer, E: ErrorLike, X, Y](
  createQueue: () => GroupingQueue[X],
  callee: X => Y,
  onDequeue: (X, Either[E, Y]) => Unit) extends DelayedQueue[C, E, X] {

  private val provide = FutureFate.hold[C, E]

  private val queue = createQueue()

  override def enqueue(value: X): Fate[C, E, Unit] = {
    provide right {
      queue enqueue value
    } flatMap {
      case true => provide.empty
      case false => update(value)
    }
  }

  private def update(value: X): Fate[C, E, Unit] = {
    val fate = provide right callee(value) transform { result =>
      val nextValue = queue dequeue value
      try {
        onDequeue(value, result)
      } catch {
        case e: Exception => Log error format(e){"[uncaught]"}
      }
      Right(nextValue)
    }
    fate flatMap {
      case Some(next) => provide await 750.millis flatMap {
        _ => update(next)
      }
      case None => provide right {
        // done
      }
    }
  }
}
