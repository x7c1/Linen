package x7c1.wheat.modern.kinds

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.chrono.HasTimer
import x7c1.wheat.modern.patch.TimerTask

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

object FutureFate {
  type ErrorLike[X] = HasConstructor[Throwable => X]
  type HasContext[X] = X => ExecutionContext

  class AppliedHolder[X: HasContext, L: ErrorLike]{
    def apply[R](f: => Either[L, R]): Fate[X, L, R] =
      Fate { x => g =>
        implicit val context = implicitly[HasContext[X]] apply x
        Future(f) recover {
          case e => Left(implicitly[ErrorLike[L]] newInstance e)
        } map g
      }

    def right[A](f: => A): Fate[X, L, A] = {
      apply(Right(f))
    }
    def await(duration: FiniteDuration)(implicit i: HasTimer[X]): Fate[X, L, Unit] =
      Fate { x => g =>
        val task = TimerTask {
          g(Right({}))
        }
        i.timer.schedule(task, duration.toMillis)
      }
  }
  def hold[X: HasContext, L: ErrorLike]: AppliedHolder[X, L] = new AppliedHolder
}
