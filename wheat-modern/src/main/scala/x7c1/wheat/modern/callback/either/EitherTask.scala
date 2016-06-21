package x7c1.wheat.modern.callback.either

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.callback.either.EitherTask.|
import x7c1.wheat.modern.patch.TaskAsync
import x7c1.wheat.modern.tasks.UiThread

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.reflectiveCalls

class EitherTask [L, R] private (f: (Either[L, R] => Unit) => Unit){

  def flatMap[A](g: R => L | A): L | A =
    EitherTask(h => f {
      case Left(x) => h(Left(x))
      case Right(x) => g(x) run h
    })

  def map[A](g: R => A): L | A =
    EitherTask(h => f {
      case Left(x) => h(Left(x))
      case Right(x) => h(Right(g(x)))
    })

  def run(g: Either[L, R] => Unit): Unit = f(g)

  def execute(): Unit = run(_ => ())

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

  def unit[L](f: => Unit): L | Unit = apply(Right(f))

  def apply[L, R](f: => Either[L, R]): L | R = apply { _(f) }

  def apply[L, R](f: (Either[L, R] => Unit) => Unit): L | R = new EitherTask(f)

  def fromEither[L, R](x: Either[L, R]): L | R = new EitherTask(_(x))

  def hold[L]: LeftApplied[L] = new LeftApplied[L]

  def await[L](msec: Int): L | Unit = {
    EitherTask(g => TaskAsync.after(msec){
      g(Right({}))
    })
  }
  def async[L, R](f: => Either[L, R]): L | R = {
    EitherTask(g => TaskAsync async { g(f) })
  }
  def ui[L, R](f: => R): L | R = EitherTask { g =>
    UiThread run g(Right(f))
  }
  def future[L: ({type F[X] = HasConstructor[Throwable => X]})#F, R]
    (f: => Either[L, R])(implicit x: ExecutionContext): L | R = {

    EitherTask { g =>
      Future(f) recover {
        case e => Left(implicitly[HasConstructor[Throwable => L]] newInstance e)
      } map g
    }
  }
}

class LeftApplied[L]{
  def apply[R](f: => Either[L, R]): L | R = EitherTask apply f
  def left(l: L): L | Nothing = EitherTask fromEither Left(l)
  def right[R](r: R): L | R = EitherTask fromEither Right(r)
  def await(msec: Int): L | Unit = EitherTask await msec
  def async[R](f: => Either[L, R]): L | R = EitherTask async f
  def ui[R](f: => R): L | R = EitherTask ui f
}
