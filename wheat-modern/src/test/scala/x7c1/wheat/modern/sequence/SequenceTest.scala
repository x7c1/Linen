package x7c1.wheat.modern.sequence

import org.scalatest.{Matchers, FlatSpecLike}

class SequenceTest extends FlatSpecLike with Matchers {

  behavior of classOf[SequenceMerger[_]].getName

  it can "merge accessors" in {
    val integers = IntegerSequence(10 to 20)
    val strings = StringSequence("a", "b", "c", "d", "e", "f")
    val inserter = new Inserter(integers, Seq(1, 2, 3))

    // 10 a 11 b c 12 d e f 13 14 15 ...
    val sequence = inserter.mergeWith(strings)
    sequence.findAt(0) shouldBe Some(Left(10))
    sequence.findAt(1) shouldBe Some(Right("a"))
    sequence.findAt(2) shouldBe Some(Left(11))

    sequence.findAt(3) shouldBe Some(Right("b"))
    sequence.findAt(4) shouldBe Some(Right("c"))
    sequence.findAt(5) shouldBe Some(Left(12))
    sequence.findAt(8) shouldBe Some(Right("f"))
    sequence.findAt(9) shouldBe Some(Right(13))

    sequence.findAt(100) shouldBe None
  }
}
