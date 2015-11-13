package x7c1.wheat.modern.kinds

trait CallbackBase[EVENT] extends ((EVENT => Unit) => Unit) {
  import scala.language.higherKinds

  type F[A] = (A => Unit) => Unit

  type This[A] <: CallbackBase[A]

  type Builder[A] = F[A] => This[A]

  def map[A: Builder](f: EVENT => A): This[A] = implicitly[Builder[A]] apply {
    (g: A => Unit) => apply(f andThen g)
  }
  def flatMap[A: Builder](f: EVENT => F[A]): This[A] = implicitly[Builder[A]] apply {
    (g: A => Unit) => apply(e => f(e) apply g)
  }
}

object CallbackTask {
  import scala.language.implicitConversions

  implicit def apply[EVENT](execute: (EVENT => Unit) => Unit): CallbackTask[EVENT] = {
    new CallbackTask(execute)
  }
}

class CallbackTask[EVENT](
  callback: (EVENT => Unit) => Unit) extends CallbackBase[EVENT] {

  override type This[A] = CallbackTask[A]

  override def apply(f: EVENT => Unit): Unit = callback(f)
}
