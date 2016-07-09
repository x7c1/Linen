package x7c1.wheat.modern.sequence

import scala.language.higherKinds

trait SequenceSlice[A, F[_] <: Sequence[_]] {
  protected def underlying: F[A]

  def slice(positions: Seq[Int])(implicit i: CanSliceFrom[F]): F[A] = {
    i.sliceFrom(underlying)(positions)
  }
}

trait CanSliceFrom[F[_]] {
  def sliceFrom[A](fa: F[A])(range: Seq[Int]): F[A]
}

private[sequence] class DefaultCanSliceFrom extends CanSliceFrom[Sequence] {
  override def sliceFrom[A](fa: Sequence[A])(positions: Seq[Int]): Sequence[A] =
    new Sequence[A] {
      lazy val positionAt = positions.lift
      override def findAt(position: Int) = {
        positionAt(position) flatMap fa.findAt
      }
      override def length = positions.length
    }
}
