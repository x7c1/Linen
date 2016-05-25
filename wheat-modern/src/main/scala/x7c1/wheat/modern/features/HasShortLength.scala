package x7c1.wheat.modern.features

trait HasShortLength[A]

object HasShortLength {
  def apply[A]: HasShortLength[A] = new HasShortLength[A]{}
}
