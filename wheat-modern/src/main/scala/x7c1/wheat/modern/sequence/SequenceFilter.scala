package x7c1.wheat.modern.sequence

import scala.annotation.tailrec
import scala.language.higherKinds

trait SequenceFilter[A, F[_] <: Sequence[_]]{
  protected def underlying: F[A]

  def filter(f: A => Boolean)(implicit x: CanFilterFrom[F]): F[A] = {
    val fa = underlying.asInstanceOf[Sequence[A]]

    @tailrec
    def loop(n: Int, xs: Vector[Int]): Vector[Int] = n match {
      case _ if n == fa.length => xs
      case _ if fa findAt n exists f => loop(n + 1, xs :+ n)
      case _ => loop(n + 1, xs)
    }
    x.asFiltered(underlying)(Sequence from loop(0, Vector()))
  }
}

trait CanFilterFrom[F[_]]{
  def asFiltered[A](fa: F[A])(positions: Sequence[Int]): F[A]
}

private[sequence] class DefaultCanFilterFrom extends CanFilterFrom[Sequence]{
  override def asFiltered[A](fa: Sequence[A])(positions: Sequence[Int]) =
    new Sequence[A] {
      override def findAt(position: Int) = positions findAt position flatMap fa.findAt
      override def length = positions.length
    }
}
