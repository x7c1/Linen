package x7c1.wheat.modern.sequence

/**
 * Provides methods excluded from default Sequence
 * because not always necessary and sometimes unsafe,
 * for example, in case of underlying with so long length.
 */

trait SequenceTraverser[A]{

  protected def underlying: Sequence[A]

  def exists(f: A => Boolean): Boolean = {
    toSeq exists f
  }
  def toSeq: Seq[A] = {
    (0 until underlying.length).view flatMap underlying.findAt
  }
}
