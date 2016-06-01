package x7c1.wheat.modern.sequence

import scala.language.higherKinds

trait SequenceMapping[A, F[_] <: Sequence[_]]{
  protected def underlying: F[A]

  def map[B](f: A => B)(implicit x: CanMapFrom[F]): F[B] = {
    x.mapFrom(underlying)(f)
  }
}

trait CanMapFrom[F[_]] {
  def mapFrom[A, B](fa: F[A])(f: A => B): F[B]
}
