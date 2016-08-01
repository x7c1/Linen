package x7c1.wheat.modern.sequence

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.modern.features.HasShortLength

class SequenceSliceTest extends FlatSpecLike with Matchers {

  behavior of classOf[SequenceSlice[_, Sequence]].getSimpleName

  implicit def regardIntAsShort = HasShortLength[Int]

  it can "slice sequence by given Seq[Int]" in {
    val xs = Sequence from Seq(11, 22, 33, 44)
    xs.slice(Seq(1,3)).toSeq shouldBe Seq(22,44)
    xs.slice(Seq(1,3)).toSeq.length shouldBe 2
    xs.slice(Seq(-1,2,100)).toSeq shouldBe Seq(33)
    xs.slice(Seq(-1,2,100)).toSeq.length shouldBe 1
  }
  it should "return empty if given index is out of range" in {
    val xs = Sequence from Seq(11, 22, 33)
    xs.slice(Seq(100)).toSeq shouldBe Seq()
  }
}
