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
  def apply[C: HasContext : HasTimer, L: ErrorLike, R, V, K](
    getKey: V => K,
    callee: V => Either[L, R],
    onDequeue: (V, Either[L, R]) => Unit ): DelayedQueue[C, L, V] = {

    new DelayedQueueImpl(getKey, callee, onDequeue)
  }
}

private class DelayedQueueImpl[C: HasContext : HasTimer, L: ErrorLike, R, V, K](
  getKey: V => K,
  callee: V => Either[L, R],
  onDequeue: (V, Either[L, R]) => Unit) extends DelayedQueue[C, L, V] {

  private val provide = FutureFate.hold[C, L]

  private val queueMap = QueueMap[K, V](getKey)

  override def enqueue(value: V): Fate[C, L, Unit] = {
    provide right synchronized {
      val exists = queueMap has getKey(value)
      queueMap enqueue value
      exists
    } flatMap {
      case true => provide.empty
      case false => update(value)
    }
  }

  private def update(value: V): Fate[C, L, Unit] = {
    val fate = provide.create(callee(value)) transform { result =>
      val key = getKey(value)
      val nextValue = this synchronized {
        queueMap dequeue key
        queueMap headOption key
      }
      try {
        onDequeue(value, result)
      } catch {
        case e: Exception => Log error format(e) {
          "[uncaught]"
        }
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
