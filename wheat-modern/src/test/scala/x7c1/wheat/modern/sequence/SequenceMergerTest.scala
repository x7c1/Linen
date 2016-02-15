package x7c1.wheat.modern.sequence

import org.scalatest.{Matchers, FlatSpecLike}

class SequenceMergerTest extends FlatSpecLike with Matchers {

  behavior of classOf[SequenceHeadline[_]].getSimpleName

  it can "generate headlined sequence" in {
    val integers = IntegerSequence(10 to 12)
    val headline = SequenceHeadline.atInterval(integers, Seq(1, 2, 3))
    val strings = StringSequence("a", "b", "c", "d", "e", "f")

    // 10 a 11 b c 12 d e f
    val sequence = headline mergeWith strings
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
    sequence.findAt(-1) shouldBe None
  }

  it should "throw exception to inconsistent length" in {
    val integers = IntegerSequence(10 to 14)
    intercept[IllegalArgumentException]{
      SequenceHeadline.atInterval(integers, Seq(1, 2, 3))
    }
  }

  it should "ignore extra items" in {
    val integers = IntegerSequence(10 to 12)
    val headline = SequenceHeadline.atInterval(integers, Seq(1, 2, 3))
    val strings = StringSequence("a", "b", "c", "d", "e", "f", "g", "h")

    // 10 a 11 b c 12 d e f
    val sequence = headline mergeWith strings
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
    sequence.findAt(9)   shouldBe None
    sequence.findAt(11)  shouldBe None
    sequence.findAt(100) shouldBe None
    sequence.findAt(-1)  shouldBe None
  }

  it should "throw exception to short sequence" in {
    val integers = IntegerSequence(10 to 12)
    val headline = SequenceHeadline.atInterval(integers, Seq(1, 2, 3))
    intercept[IllegalArgumentException]{
      headline mergeWith StringSequence("a", "b", "c", "d", "e")
    }
  }

}
