package x7c1.wheat.modern.callback.either

import x7c1.wheat.modern.callback.either.EitherTask.|
import x7c1.wheat.modern.patch.TaskAsync

import scala.concurrent.{Future, Promise}

class EitherTask [L, R](f: (Either[L, R] => Unit) => Unit){

  def flatMap[A](g: R => L | A): L | A =
    new EitherTask[L, A](h => f {
      case Left(x) => h(Left(x))
      case Right(x) => g(x) run h
    })

  def map[A](g: R => A): L | A =
    new EitherTask[L, A](h => f {
      case Left(x) => h(Left(x))
      case Right(x) => h(Right(g(x)))
    })

  def run(g: Either[L, R] => Unit): Unit = f(g)

  def toFuture: Future[Either[L, R]] = {
    val promise = Promise[Either[L, R]]()
    f { either =>
      try promise trySuccess either
      catch { case e: Throwable => promise tryFailure e }
    }
    promise.future
  }
}

object EitherTask {
  type | [A, B] = EitherTask[A, B]

  def fromEither[L, R](x: Either[L, R]): L | R = new EitherTask(_(x))
  def through[L]: LeftApplied[L] = new LeftApplied[L]
}

class LeftApplied[L]{
  def left(l: L): L | Nothing = EitherTask fromEither Left(l)
  def right[R](r: R): L | R = EitherTask fromEither Right(r)
  def await(msec: Int): L | Unit =
    new EitherTask(g => TaskAsync.after(msec){
      g(Right({}))
    })
}
