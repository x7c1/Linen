package x7c1.wheat.modern.kinds

import java.util.Timer

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.features.HasValue
import x7c1.wheat.modern.patch.TimerTask

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}

object FutureFate {
  type ErrorLike[X] = HasConstructor[Throwable => X]

  type HasContext[X] = HasValue[X => ExecutionContext]

  type HasTimer[X] = HasValue[X => Timer]

  def fromEither[X: HasContext, L: ErrorLike, R](f: => Either[L, R]): Fate[X, L, R] =
    Fate { x => g =>
      implicit val context = implicitly[HasContext[X]].value(x)
      Future(f) recover {
        case e => Left(implicitly[ErrorLike[L]] newInstance e)
      } map g
    }

  def fromCallback[X: HasContext, L: ErrorLike, R](callback: CallbackTask[R]): Fate[X, L, R] = {
    Fate { x => g =>
      implicit val context = implicitly[HasContext[X]].value(x)
      Future[CallbackTask[Either[L, R]]] {
        callback map Right.apply
      } recover {
        case e => task {
          Left(implicitly[ErrorLike[L]] newInstance e)
        }
      } map (_(g))
    }
  }
  def fromPromise[X: HasContext, L: ErrorLike, R](promise: Promise[R]): Fate[X, L, R] = {
    Fate { x => g =>
      implicit val context = implicitly[HasContext[X]].value(x)
      promise.future map Right.apply recover {
        case e => Left(implicitly[ErrorLike[L]] newInstance e)
      } map g
    }
  }
  def on[X: HasContext]: Applied1[X] = new Applied1

  def hold[X: HasContext, L: ErrorLike]: Applied2[X, L] = new Applied2

  class Applied1[X: HasContext]{
    def create[L: ErrorLike, R](f: => Either[L, R]): Fate[X, L, R] = {
      FutureFate.fromEither[X, L, R](f)
    }
    def await[L: ErrorLike](duration: FiniteDuration)(implicit i: HasTimer[X]): Fate[X, L, Unit] =
      Fate { x => g =>
        val task = TimerTask {
          g(Right({}))
        }
        i.value(x).schedule(task, duration.toMillis)
      }
  }
  class Applied2[X: HasContext, L: ErrorLike]{
    def create[R](f: => Either[L, R]): Fate[X, L, R] = FutureFate fromEither f

    def fromCallback[R](callback: CallbackTask[R]): Fate[X, L, R] = {
      FutureFate.fromCallback(callback)
    }
    def fromPromise[R](promise: Promise[R]): Fate[X, L, R] = {
      FutureFate.fromPromise[X, L, R](promise)
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
        i.value(x).schedule(task, duration.toMillis)
      }
  }
}
