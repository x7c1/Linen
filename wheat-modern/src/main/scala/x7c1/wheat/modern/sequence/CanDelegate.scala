package x7c1.wheat.modern.sequence

import scala.language.higherKinds

trait CanDelegate[F[_]]{
  def delegate[A, B](from: F[A])(to: Sequence[B]): F[B]
}

private[sequence] class DefaultCanDelegate extends CanDelegate[Sequence]{
  override def delegate[A, B](from: Sequence[A])(to: Sequence[B]) = to
}
