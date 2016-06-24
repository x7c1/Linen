package x7c1.wheat.modern.kinds

import scala.language.reflectiveCalls

trait Fate[X, +L, +R]{
  def map[R2](f: R => R2): Fate[X, L, R2]
  def flatMap[L2 >: L, R2](f: R => Fate[X, L2, R2]): Fate[X, L2, R2]
  def run[L2 >: L, R2 >: R]: X => (Either[L2, R2] => Unit) => Unit
}

object Fate {
  def apply[X, L, R](underlying: X => (Either[L, R] => Unit) => Unit): Fate[X, L, R] = {
    new FateImpl(underlying)
  }
}

private class FateImpl[X, L, R](
  underlying: X => (Either[L, R] => Unit) => Unit) extends Fate[X, L, R]{

  override def map[R2](f: R => R2): Fate[X, L, R2] = new FateImpl[X, L, R2](
    context => g => underlying(context){
      case Right(right) => g(Right(f(right)))
      case Left(left) => g(Left(left))
    }
  )
  override def flatMap[L2 >: L, R2](f: R => Fate[X, L2, R2]): Fate[X, L2, R2] = new FateImpl[X, L2, R2](
    context => g => underlying(context){
      case Right(right) => f(right).run(context)(g)
      case Left(left) => g(Left(left))
    }
  )
  override def run[L2 >: L, R2 >: R] = {
    underlying
  }
}
