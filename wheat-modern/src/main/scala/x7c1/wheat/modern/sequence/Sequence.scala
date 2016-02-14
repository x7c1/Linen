package x7c1.wheat.modern.sequence

import scala.annotation.tailrec

trait Sequence[A]{
  def length: Int
  def findAt(position: Int): Option[A]
}

trait SequenceMerger[A] {
  def mergeWith[B](accessor2: Sequence[B]): Sequence[Either[A, B]]
}

class Inserter[A](
  accessor1: Sequence[A],
  interval: Seq[Int]) extends SequenceMerger[A]{

  override def mergeWith[B](accessor2: Sequence[B]): Sequence[Either[A, B]] = {
    new Sequence[Either[A, B]] {
      override def length: Int = accessor1.length + accessor2.length
      override def findAt(position: Int): Option[Either[A, B]] = {
        val xs = interval.scanLeft(0){ (n, sum) => n + sum + 1 }
        println(xs)

        ???
      }
    }
  }
}

object IntegerSequence {
  def apply(range: Range): Sequence[Int] = new IntegerSequence(range)
}

private class IntegerSequence(range: Range) extends Sequence[Int] {
  override def length: Int = range.length
  override def findAt(position: Int): Option[Int] = range.drop(position).headOption
}

object StringSequence {
  def apply(strings: String*): Sequence[String] = new StringSequence(strings:_*)
}

private class StringSequence(strings: String*) extends Sequence[String]{
  override def length: Int = strings.length
  override def findAt(position: Int): Option[String] = strings.drop(position).headOption
}
