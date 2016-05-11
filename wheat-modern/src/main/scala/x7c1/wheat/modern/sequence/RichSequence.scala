package x7c1.wheat.modern.sequence

/**
 * Provides methods excluded from default Sequence
 * because not always necessary and sometimes unsafe,
 * for example, in case of underlying with so long length.
 */

trait RichSequence[A]{

  protected def underlying: Sequence[A]

  def exists(f: A => Boolean): Boolean = {
    toSeq exists f
  }
  def toSeq: Seq[A] = {
    (0 to underlying.length - 1).view flatMap underlying.findAt
  }
}

trait AllowRichSequence[A]{
  implicit class RichSequenceImpl(
    override protected val underlying: Sequence[A]) extends RichSequence[A]
}
