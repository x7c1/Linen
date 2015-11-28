package x7c1.wheat.modern.kinds

trait CallbackBase[EVENT] extends ((EVENT => Unit) => Unit) {
  import scala.language.higherKinds

  type This[A] <: CallbackBase[A]

  type Builder[A] = ((A => Unit) => Unit) => This[A]

  def map[A: Builder](f: EVENT => A): This[A] = implicitly[Builder[A]] apply {
    g => apply(f andThen g)
  }
  def flatMap[A: Builder](f: EVENT => This[A]): This[A] = implicitly[Builder[A]] apply {
    g => apply(e => f(e) apply g)
  }
  def withFilter(f: EVENT => Boolean)(implicit x: Builder[EVENT]): This[EVENT] = x apply {
    g => apply(e => if (f(e)) g(e))
  }
}
