package x7c1.wheat.modern.queue.map

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.fate.FateProvider.{ErrorLike, HasContext}
import x7c1.wheat.modern.fate.FutureFate
import x7c1.wheat.modern.fate.FutureFate.HasTimer
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.kinds.Fate

import scala.concurrent.duration.DurationInt

private trait DelayedQueue[C, L, V] {

  def enqueue(value: V): Fate[C, L, Unit]
}

private object DelayedQueue {
  def apply[C: HasContext : HasTimer, L: ErrorLike, R, V](
    queue: ValueQueue[V],
    callee: V => Either[L, R],
    onDequeue: (V, Either[L, R]) => Unit ): DelayedQueue[C, L, V] = {

    new DelayedQueueImpl(queue, callee, onDequeue)
  }
}

private class DelayedQueueImpl[C: HasContext : HasTimer, L: ErrorLike, R, V](
  queue: ValueQueue[V],
  callee: V => Either[L, R],
  onDequeue: (V, Either[L, R]) => Unit) extends DelayedQueue[C, L, V] {

  private val provide = FutureFate.hold[C, L]

  override def enqueue(value: V): Fate[C, L, Unit] = {
    provide right synchronized {
      queue enqueue value
    } flatMap {
      case true => provide.empty
      case false => update(value)
    }
  }

  private def update(value: V): Fate[C, L, Unit] = {
    val fate = provide.create(callee(value)) transform { result =>
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
        // done: dispatch something?
      }
    }
  }
}
