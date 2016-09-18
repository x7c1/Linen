package x7c1.wheat.modern.fate

import java.util.Timer

import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.fate.FateProvider.{ErrorLike, HasContext}
import x7c1.wheat.modern.features.HasInstance
import x7c1.wheat.modern.kinds.{Fate, FateRunner}
import x7c1.wheat.modern.patch.TimerTask

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

object FutureFate {

  type HasTimer[X] = HasInstance[X => Timer]

  def fromEither[X: HasContext, L: ErrorLike, R](f: => Either[L, R]): Fate[X, L, R] =
    Fate { x => g =>
      implicit val context = implicitly[HasContext[X]].instance(x)
      Future(f) onComplete {
        case Success(either) => g(either)
        case Failure(e) => g(Left(implicitly[ErrorLike[L]] newInstance e))
      }
    }

  def fromCallback[X: HasContext, L: ErrorLike, R](callback: CallbackTask[R]): Fate[X, L, R] = {
    Fate { x => g =>
      implicit val context = implicitly[HasContext[X]].instance(x)
      callback.toFuture onComplete {
        case Success(r) => g(Right(r))
        case Failure(e) => g(Left(implicitly[ErrorLike[L]] newInstance e))
      }
    }
  }

  def fromPromise[X: HasContext, L: ErrorLike, R](promise: Promise[R]): Fate[X, L, R] = {
    Fate { x => g =>
      implicit val context = implicitly[HasContext[X]].instance(x)
      promise.future onComplete {
        case Success(r) => g(Right(r))
        case Failure(e) => g(Left(implicitly[ErrorLike[L]] newInstance e))
      }
    }
  }

  def on[X: HasContext]: Applied1[X] = new Applied1

  def hold[X: HasContext, L: ErrorLike]: Applied2[X, L] = new Applied2

  class Applied1[X: HasContext] {
    def create[L: ErrorLike, R](f: => Either[L, R]): Fate[X, L, R] = {
      FutureFate.fromEither[X, L, R](f)
    }

    def await[L: ErrorLike](duration: FiniteDuration)(implicit i: HasTimer[X]): Fate[X, L, Unit] =
      Fate { x => g =>
        val task = TimerTask {
          g(Right({}))
        }
        i.instance(x).schedule(task, duration.toMillis)
      }
  }

  class Applied2[X: HasContext, L: ErrorLike] {
    def create[R](f: => Either[L, R]): Fate[X, L, R] = FutureFate fromEither f

    def fromCallback[R](callback: CallbackTask[R]): Fate[X, L, R] = {
      FutureFate.fromCallback(callback)
    }

    def fromPromise[R](promise: Promise[R]): Fate[X, L, R] = {
      FutureFate.fromPromise[X, L, R](promise)
    }

    def partially[T, R](f: T => R): T => Fate[X, L, R] = t => {
      try {
        right(f(t))
      } catch {
        case e: Throwable =>
          create(Left(implicitly[ErrorLike[L]] newInstance e))
      }
    }

    def right[A](f: => A): Fate[X, L, A] = {
      create(Right(f))
    }

    def empty: Fate[X, L, Unit] = {
      right({})
    }

    def await(duration: FiniteDuration)(implicit i: HasTimer[X]): Fate[X, L, Unit] =
      Fate { x => g =>
        val task = TimerTask {
          g(Right({}))
        }
        i.instance(x).schedule(task, duration.toMillis)
      }
  }

}
