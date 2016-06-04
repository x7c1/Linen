package x7c1.wheat.modern.sequence

import scala.annotation.tailrec
import scala.language.higherKinds

trait SequenceFilter[A, F[_] <: Sequence[_]]{
  protected def underlying: F[A]

  def filter(f: A => Boolean)(implicit x: CanFilterFrom[F]): F[A] = {
    val fa = underlying.asInstanceOf[Sequence[A]]

    @tailrec
    def loop(n: Int, hit: Int, map: Map[Int, Int]): Map[Int, Int] = n match {
      case _ if n == fa.length => map
      case _ if fa findAt n exists f => loop(n + 1, hit + 1, map + (hit -> n))
      case _ => loop(n + 1, hit, map)
    }
    x.asFiltered(underlying)(loop(0, 0, Map()))
  }
}

trait CanFilterFrom[F[_]]{
  def asFiltered[A](fa: F[A])(filtered: Map[Int, Int]): F[A]
}

private[sequence] class DefaultCanFilterFrom extends CanFilterFrom[Sequence]{
  override def asFiltered[A](fa: Sequence[A])(filtered: Map[Int, Int]) =
    new Sequence[A] {
      override def findAt(position: Int) = filtered get position flatMap fa.findAt
      override def length = filtered.size
    }
}
