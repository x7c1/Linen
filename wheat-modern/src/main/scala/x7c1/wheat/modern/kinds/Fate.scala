package x7c1.wheat.modern.kinds

import scala.language.reflectiveCalls

trait Fate[L, R, X]{
  def map[R2](f: R => R2): Fate[L, R2, X]
  def flatMap[R2](f: R => Fate[L, R2, X]): Fate[L, R2, X]
  def run(x: X)(f: Either[L, R] => Unit): Unit
}

private class FateImpl[L, R, X](
  underlying: X => (Either[L, R] => Unit) => Unit) extends Fate[L, R, X]{

  override def map[R2](f: R => R2): Fate[L, R2, X] = new FateImpl[L, R2, X](
    context => g => underlying(context){ a =>
      g(a.right map f)
    }
  )
  override def flatMap[R2](f: R => Fate[L, R2, X]): Fate[L, R2, X] = new FateImpl[L, R2, X](
    context => g => underlying(context){
      case Left(e) => g(Left(e))
      case Right(a) => f(a).run(context)(g)
    }
  )
  override def run(context: X)(f: Either[L, R] => Unit): Unit = {
    underlying(context)(f)
  }
}
