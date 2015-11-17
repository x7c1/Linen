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
}

object CallbackTask {
  import scala.language.implicitConversions

  implicit def apply[EVENT](execute: (EVENT => Unit) => Unit): CallbackTask[EVENT] = {
    new CallbackTask(execute)
  }
  def taskOf[A](f: (A => Unit) => Unit): CallbackTask[A] = CallbackTask(f)
}

class CallbackTask[EVENT](
  callback: (EVENT => Unit) => Unit) extends CallbackBase[EVENT] {

  override type This[A] = CallbackTask[A]

  override def apply(f: EVENT => Unit): Unit = callback(f)

  def apply(): Unit = callback(_ => ())
}