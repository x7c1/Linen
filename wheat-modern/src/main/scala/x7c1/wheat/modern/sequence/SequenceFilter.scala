package x7c1.wheat.modern.sequence

import scala.language.higherKinds

trait SequenceFilter[A, F[_] <: Sequence[_]]{
  protected def underlying: F[A]

  def filter(f: A => Boolean)(implicit x: CanFilterFrom[F]): F[A] = {
    x.filterFrom(underlying)(f)
  }
}

trait CanFilterFrom[F[_]]{
  def filterFrom[A](fa: F[A])(f: A => Boolean): F[A]
}
