package x7c1.linen.testing

import x7c1.wheat.modern.sequence.{Sequence, RichSequence}

trait HasRichSequence {
  implicit class TestingRichSequence[A](
    override protected val underlying: Sequence[A]) extends RichSequence[A]
}
