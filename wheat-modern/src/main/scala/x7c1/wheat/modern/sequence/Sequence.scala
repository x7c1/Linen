package x7c1.wheat.modern.sequence

import scala.annotation.tailrec

trait Sequence[A]{
  def length: Int
  def findAt(position: Int): Option[A]
}

trait SequenceMerger[A] {
  def mergeWith[B](sequence: Sequence[B]): Sequence[Either[A, B]]
}

class SequenceHeadline[A] private (
  sequence1: Sequence[A],
  interval: Seq[Int]) extends SequenceMerger[A]{

  require(sequence1.length == interval.length)

  override def mergeWith[B](sequence2: Sequence[B]): Sequence[Either[A, B]] = {
    require(
      requirement = (sequence1.length + sequence2.length) >= intervals.last,
      message = "sequence.length too short"
    )
    new Sequence[Either[A, B]] {
      override def findAt(position: Int): Option[Either[A, B]] = {
        @tailrec
        def loop(inf: Int, sup: Int): Option[Either[A, B]] = {
          val mid = (inf + sup) / 2
          //println(s"ys:$ys, inf:$inf, sup:$sup, mid:$mid")

          intervals match {
            case _ if sup == inf => position - intervals(inf) match {
              case x if x > 0 => None
              case 0 => sequence1 findAt inf map Left.apply
              case _ => sequence2 findAt (position - inf) map Right.apply
            }
            case _ if intervals(mid) < position  => loop(mid + 1, sup)
            case _ => loop(inf, mid)
          }
        }
        loop(0, intervals.length - 1)
      }
      override def length: Int = intervals.last
    }
  }
  private lazy val intervals = interval.scanLeft(0){ _ + _ + 1 }
}

object SequenceHeadline {
  def atInterval[A](sequence: Sequence[A], interval: Seq[Int]): SequenceHeadline[A] = {
    new SequenceHeadline(sequence, interval)
  }
}

object IntegerSequence {
  def apply(range: Range): Sequence[Int] = new IntegerSequence(range)
}

private class IntegerSequence(range: Range) extends Sequence[Int] {
  override def length: Int = range.length
  override def findAt(position: Int): Option[Int] =
    if (range.isDefinedAt(position)) Some(range(position))
    else None
}

object StringSequence {
  def apply(strings: String*): Sequence[String] = new StringSequence(strings:_*)
}

private class StringSequence(strings: String*) extends Sequence[String]{
  override def length: Int = strings.length
  override def findAt(position: Int): Option[String] =
    if (strings.isDefinedAt(position)) Some(strings(position))
    else None
}
