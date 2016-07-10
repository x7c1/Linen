package x7c1.wheat.modern.sequence

import x7c1.wheat.modern.features.HasShortLength

import scala.annotation.tailrec

object HeadlineSequencer {
  def apply[X: HasShortLength, A](
    equals: (X, X) => Boolean,
    toHeadline: Sequence[X] => A): HeadlineSequencer[X, A] = {

    new HeadlineSequencer(equals, toHeadline)
  }
}

class HeadlineSequencer[X, A] private(
  equals: (X, X) => Boolean,
  toHeadline: Sequence[X] => A) {

  def derive(original: Sequence[X]): Sequence[Either[A, X]] = {
    @tailrec
    def loop(n: Int, prev: Option[X], list: Vector[Int], result: Vector[Seq[Int]]): Vector[Seq[Int]] = {
      val current = original findAt n
      (prev, current) match {
        case (None, None) =>
          result
        case (Some(_), None) =>
          result :+ list
        case (Some(x1), Some(x2)) if ! equals(x1, x2) =>
          loop(n + 1, current, Vector(n), result :+ list)
        case _ =>
          loop(n + 1, current, list :+ n, result)
      }
    }
    val positionsList = loop(0, None, Vector(), Vector())
    val sequence = new Sequence[A] {
      private lazy val find = positionsList.lift
      override def findAt(n: Int) = {
        find(n) map { positions =>
          toHeadline(original slice positions)
        }
      }
      override def length = positionsList.length
    }
    val interval = positionsList map { _.length }
    val headlines = SequenceHeadlines.atInterval(sequence, interval)
    headlines mergeWith original
  }

}
