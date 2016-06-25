package x7c1.wheat.modern.kinds

import java.util.Timer

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.features.{HasSharedInstance, HasValue}
import x7c1.wheat.modern.patch.TimerTask

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

object FutureFate {
  type ErrorLike[X] = HasConstructor[Throwable => X]
  type HasContext[X] = HasValue[X => ExecutionContext]

  def apply[X: HasContext, L: ErrorLike, R](f: => Either[L, R]): Fate[X, L, R] =
    Fate { x => g =>
      implicit val context = implicitly[HasContext[X]].value(x)
      Future(f) recover {
        case e => Left(implicitly[ErrorLike[L]] newInstance e)
      } map g
    }

  def on[X: HasContext]: Applied1[X] = new Applied1

  def hold[X: HasContext, L: ErrorLike]: Applied2[X, L] = new Applied2

  class Applied1[X: HasContext]{
    def create[L: ErrorLike, R](f: => Either[L, R]): Fate[X, L, R] = {
      FutureFate[X, L, R](f)
    }
  }
  class Applied2[X: HasContext, L: ErrorLike]{
    def create[R](f: => Either[L, R]): Fate[X, L, R] = FutureFate(f)

    def right[A](f: => A): Fate[X, L, A] = {
      create(Right(f))
    }
    def await(duration: FiniteDuration)(implicit i: HasSharedInstance[X, Timer]): Fate[X, L, Unit] =
      Fate { x => g =>
        val task = TimerTask {
          g(Right({}))
        }
        i.instance.schedule(task, duration.toMillis)
      }
  }

}
