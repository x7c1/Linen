package x7c1.wheat.modern.sequence

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.modern.features.HasShortLength

class SequenceFilterTest extends FlatSpecLike with Matchers {

  behavior of classOf[SequenceFilter[_, Sequence]].getSimpleName

  implicit def short[A] = HasShortLength[A]

  it can "filter sequence by given function" in {
    val sequence = Sequence from Seq(11,22,33,44,55,66,88,99)
    sequence.filter(_ % 2 == 0).toSeq shouldBe Seq(22,44,66,88)
  }
}
