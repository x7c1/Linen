package x7c1.wheat.modern.sequence

/**
 * Provides methods excluded from default Sequence
 * because not always necessary and sometimes unsafe,
 * for example, in case of underlying with so long length.
 */

trait RichSequence[A]{

  protected def underlying: Sequence[A]

  def exists(f: A => Boolean): Boolean = {
    (0 to underlying.length - 1).view.flatMap(underlying.findAt).exists(f)
  }
}
