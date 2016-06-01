package x7c1.wheat.modern.sequence

import scala.collection.mutable.ListBuffer
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

private[sequence] class DefaultCanFilterFrom extends CanFilterFrom[Sequence]{
  override def filterFrom[A](fa: Sequence[A])(f: A => Boolean) =
    new Sequence[A] {
      val (find, size) = {
        val cache = ListBuffer[Int]()
        (0 until fa.length).view.foreach {
          case n if fa findAt n exists f => cache += n
          case  _ => //nop
        }
        cache.lift -> cache.length
      }
      override def findAt(position: Int): Option[A] = {
        find(position) flatMap fa.findAt
      }
      override def length: Int = size
    }
}
