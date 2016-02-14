package x7c1.wheat.modern.sequence

import org.scalatest.{Matchers, FlatSpecLike}

class SequenceMergerTest extends FlatSpecLike with Matchers {

  behavior of classOf[SequenceMerger[_]].getName

  val strings = StringSequence("a", "b", "c", "d", "e", "f")

  it can "merge sequence(1)" in {
    val integers = IntegerSequence(10 to 12)
    val separator = new SequenceSeparator(integers, Seq(1, 2, 3))

    // 10 a 11 b c 12 d e f
    val sequence = separator.mergeWith(strings)
    sequence.length shouldBe 9

    sequence.findAt(0) shouldBe Some(Left(10))
    sequence.findAt(1) shouldBe Some(Right("a"))
    sequence.findAt(2) shouldBe Some(Left(11))

    sequence.findAt(3) shouldBe Some(Right("b"))
    sequence.findAt(4) shouldBe Some(Right("c"))
    sequence.findAt(5) shouldBe Some(Left(12))
    sequence.findAt(6) shouldBe Some(Right("d"))
    sequence.findAt(7) shouldBe Some(Right("e"))
    sequence.findAt(8) shouldBe Some(Right("f"))
    sequence.findAt(9) shouldBe None
    sequence.findAt(10) shouldBe None
    sequence.findAt(100) shouldBe None
  }

  it can "merge sequence(2)" in {
    val integers = IntegerSequence(10 to 14)
    val separator = new SequenceSeparator(integers, Seq(1, 2, 3))

    // 10 a 11 b c 12 d e f 13
    val sequence = separator.mergeWith(strings)
    sequence.length shouldBe 11

    sequence.findAt(0) shouldBe Some(Left(10))
    sequence.findAt(1) shouldBe Some(Right("a"))
    sequence.findAt(2) shouldBe Some(Left(11))

    sequence.findAt(3) shouldBe Some(Right("b"))
    sequence.findAt(4) shouldBe Some(Right("c"))
    sequence.findAt(5) shouldBe Some(Left(12))
    sequence.findAt(6) shouldBe Some(Right("d"))
    sequence.findAt(7) shouldBe Some(Right("e"))
    sequence.findAt(8) shouldBe Some(Right("f"))
    sequence.findAt(9) shouldBe Some(Left(13))
    sequence.findAt(10) shouldBe None
    sequence.findAt(100) shouldBe None
  }

}
