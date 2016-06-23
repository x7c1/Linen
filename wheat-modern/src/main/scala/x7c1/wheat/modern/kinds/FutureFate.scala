package x7c1.wheat.modern.kinds

import x7c1.wheat.macros.reify.{HasConstructor, HasInstance}

import scala.concurrent.{ExecutionContext, Future}

object FutureFate {
  type ErrorLike[X] = HasConstructor[Throwable => X]
  type HasContext[X] = HasInstance[X => ExecutionContext]

  class AppliedHolder[L: ErrorLike, X: HasContext]{
    def apply[R](f: => Either[L, R]): Fate[L, R, X] =
      new FateImpl( x => g => {
        implicit val context = implicitly[HasContext[X]] instanceOf x
        val future = Future(f) recover {
          case e => Left(implicitly[ErrorLike[L]] newInstance e)
        }
        future map g
      })
  }
  def hold[L: ErrorLike, X: HasContext]: AppliedHolder[L, X] = new AppliedHolder
}
