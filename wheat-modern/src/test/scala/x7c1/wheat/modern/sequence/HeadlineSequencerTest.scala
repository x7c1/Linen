package x7c1.wheat.modern.sequence

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.modern.features.HasShortLength

class HeadlineSequencerTest extends FlatSpecLike with Matchers {

  behavior of classOf[HeadlineSequencer[_, _]].getSimpleName

  implicit def regardEitherAsShort = HasShortLength[Either[String, Char]]

  implicit def regardIntAsShort = HasShortLength[Char]

  it can "generate new sequence with headlines" in {
    val sequencer = HeadlineSequencer[Char, String](
      equals = _ == _,
      toHeadline = xs => s"label(${xs.length})"
    )
    val sequence = sequencer derive Sequence.from(Seq('a', 'b', 'b', 'c', 'c', 'c'))
    sequence.toSeq shouldBe Seq(
      Left("label(1)"),
      Right('a'),
      Left("label(2)"),
      Right('b'), Right('b'),
      Left("label(3)"),
      Right('c'), Right('c'), Right('c')
    )
    val sequence2 = sequencer derive Sequence.from(Seq('a'))
    sequence2.toSeq shouldBe Seq(
      Left("label(1)"),
      Right('a')
    )
    val sequence3 = sequencer derive Sequence.from(Seq('a', 'b', 'a', 'a', 'a'))
    sequence3.toSeq shouldBe Seq(
      Left("label(1)"),
      Right('a'),
      Left("label(1)"),
      Right('b'),
      Left("label(3)"),
      Right('a'), Right('a'), Right('a')
    )
  }
  it should "generate empty Sequence if given Sequence is empty" in {
    val sequencer = HeadlineSequencer[Char, String](
      equals = _ == _,
      toHeadline = xs => s"label(${xs.length})"
    )
    val sequence = sequencer.derive[Sequence](Sequence.from(Seq()))
    sequence.toSeq shouldBe Seq()
  }
}
