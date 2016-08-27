package x7c1.wheat.modern.queue.map

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.fate.FateProvider.{ErrorLike, HasContext}
import x7c1.wheat.modern.fate.FutureFate
import x7c1.wheat.modern.fate.FutureFate.HasTimer
import x7c1.wheat.modern.kinds.Fate
import x7c1.wheat.modern.queue.map.DistributedQueue.{CanDump, ThrowableLike}

import scala.collection.mutable
import scala.concurrent.Promise

trait DistributedQueue[C, L, X, Y] {

  def enqueue(value: X): Fate[C, L, Y]
}

object DistributedQueue {
  trait ThrowableLike[X] {
    def throwableFrom(x: X): Throwable
  }
  trait CanDump[X] {
    def dump(x: X): String
  }
}

private class DistributedQueueImpl[
  C: HasContext : HasTimer,
  L: ThrowableLike : ErrorLike,
  X: CanDump, Y, K
](
  getGroupKey: X => K,
  callee: X => Either[L, Y]
) extends DistributedQueue[C, L, X, Y] {

  private val map = mutable.Map[X, Promise[Y]]()

  private val queue: DelayedQueue[C, L, X] = {
    DelayedQueue(getGroupKey, callee, onDequeue)
  }

  private val provide = FutureFate.hold[C, L]

  override def enqueue(value: X): Fate[C, L, Y] = synchronized {
    map get value match {
      case Some(existent) =>
        provide fromPromise existent
      case None =>
        val promise = Promise[Y]()
        map(value) = promise
        queue.enqueue(value) flatMap { unit =>
          provide fromPromise promise
        }
    }
  }

  private lazy val onDequeue: (X, Either[L, Y]) => Unit = (x, either) => synchronized {
    map remove x match {
      case Some(promise) => either match {
        case Left(e) =>
          promise failure implicitly[ThrowableLike[L]].throwableFrom(e)
        case Right(y) =>
          promise success y
      }
      case None =>
        val dumped = implicitly[CanDump[X]] dump x
        Log warn s"not enqueued or already dequeued [$dumped]"
    }
  }
}