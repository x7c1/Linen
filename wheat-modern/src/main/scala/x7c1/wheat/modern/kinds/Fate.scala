package x7c1.wheat.modern.kinds

import x7c1.wheat.modern.callback.either.EitherTask

import scala.language.reflectiveCalls

trait Fate[X, +L, +R] {
  def map[R2](f: R => R2): Fate[X, L, R2]

  def flatMap[L2 >: L, R2](f: R => Fate[X, L2, R2]): Fate[X, L2, R2]

  def run[L2 >: L, R2 >: R](x: X): FateRunner[L2, R2]

  def transform[L2, R2](f: Either[L, R] => Either[L2, R2]): Fate[X, L2, R2]

  def toEitherTask[L2 >: L, R2 >: R](x: X): EitherTask[L2, R2] = EitherTask(run(x))
}

object Fate {
  def apply[X, L, R](underlying: X => (Either[L, R] => Unit) => Unit): Fate[X, L, R] = {
    new FateImpl(underlying)
  }

  def apply[X, L, R](r: R): Fate[X, L, R] = Fate { x => g =>
    g(Right(r))
  }

  implicit class Fates[X, L, R](fates: Seq[Fate[X, L, R]]) {
    def toParallel: Fate[X, Seq[L], Seq[R]] = Fate { x => f =>
      val dispatcher = new EitherCollector(f, fates.length)
      fates foreach {
        _ run x apply dispatcher.run
      }
    }
  }

}

class FateRunner[L, R](
  underlying: (Either[L, R] => Unit) => Unit) extends ((Either[L, R] => Unit) => Unit) {

  override def apply(f: Either[L, R] => Unit): Unit = underlying(f)

  def atLeft(f: L => Unit): Unit = apply {
    case Right(r) => //nop
    case Left(l) => f(l)
  }
}

private class FateImpl[X, L, R](
  underlying: X => (Either[L, R] => Unit) => Unit) extends Fate[X, L, R] {

  override def map[R2](f: R => R2): Fate[X, L, R2] = new FateImpl[X, L, R2](
    context => g => underlying(context) {
      case Right(right) => g(Right(f(right)))
      case Left(left) => g(Left(left))
    }
  )

  override def flatMap[L2 >: L, R2](f: R => Fate[X, L2, R2]): Fate[X, L2, R2] = new FateImpl[X, L2, R2](
    context => g => underlying(context) {
      case Right(right) => f(right).run(context)(g)
      case Left(left) => g(Left(left))
    }
  )

  override def run[L2 >: L, R2 >: R](x: X) = {
    new FateRunner[L2, R2](underlying(x))
  }

  override def transform[L2, R2](f: Either[L, R] => Either[L2, R2]): Fate[X, L2, R2] = {
    new FateImpl[X, L2, R2](
      context => g =>
        underlying(context) {
          g apply f(_)
        }
    )
  }
}

private class EitherCollector[L, R](f: Either[Seq[L], Seq[R]] => Unit, threshold: Int) {

  import java.util.concurrent.atomic.AtomicInteger
  import scala.collection.mutable.ArrayBuffer

  private val lefts = ArrayBuffer[L]()

  private val rights = ArrayBuffer[R]()

  private val processed = new AtomicInteger(0)

  def run(either: Either[L, R]): Unit = synchronized {
    either match {
      case Left(l) => lefts += l
      case Right(r) => rights += r
    }
    if (processed.incrementAndGet() == threshold) {
      if (lefts.nonEmpty) f(Left(lefts))
      else f(Right(rights))
    }
  }
}
