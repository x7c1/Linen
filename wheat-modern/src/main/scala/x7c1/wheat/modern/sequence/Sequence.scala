package x7c1.wheat.modern.sequence

import x7c1.wheat.modern.features.HasShortLength

import scala.language.higherKinds

trait Sequence[+A]{
  def length: Int
  def findAt(position: Int): Option[A]
}

object Sequence {

  def from[A](xs: Seq[A]): Sequence[A] = new Sequence[A] {
    override def findAt(position: Int) = position match {
      case x if xs isDefinedAt x => Some(xs(position))
      case _ => None
    }
    override def length: Int = xs.length
  }
  implicit class traverse[A: HasShortLength](
    override protected val underlying: Sequence[A]) extends SequenceTraverser[A]

  implicit class map[A, F[_] <: Sequence[_]](
    override protected val underlying: F[A]) extends SequenceMapping[A, F]

  implicit object canMapFrom extends DefaultCanMapFrom

  implicit class filter[A: HasShortLength, F[_] <: Sequence[_]](
    override protected val underlying: F[A]) extends SequenceFilter[A, F]

  implicit object canFilterFrom extends DefaultCanFilterFrom

  implicit class slice[A, F[_] <: Sequence[_]](
    override protected val underlying: F[A] ) extends SequenceSlice[A, F]

  implicit object canSliceFrom extends DefaultCanSliceFrom

  implicit object canDelegate extends DefaultCanDelegate
}
