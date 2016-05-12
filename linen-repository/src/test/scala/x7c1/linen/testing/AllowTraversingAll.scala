package x7c1.linen.testing

import x7c1.wheat.modern.sequence.{Sequence, SequenceTraverser}

trait AllowTraversingAll {
  implicit class SequenceTraverserImpl[A](
    override protected val underlying: Sequence[A]) extends SequenceTraverser[A]
}
