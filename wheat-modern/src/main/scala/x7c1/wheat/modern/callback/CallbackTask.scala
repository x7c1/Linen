package x7c1.wheat.modern.callback

import java.io.Closeable

import x7c1.wheat.modern.kinds.CallbackBase

class CallbackTask[EVENT](
  callback: (EVENT => Unit) => Unit) extends CallbackBase[EVENT] {

  override type This[A] = CallbackTask[A]

  override def apply(f: EVENT => Unit): Unit = callback(f)

  def execute(): Unit = callback(_ => ())
}

object CallbackTask {
  import scala.language.implicitConversions

  implicit def apply[EVENT](execute: (EVENT => Unit) => Unit): CallbackTask[EVENT] = {
    new CallbackTask(execute)
  }
  def task = TaskProvider

  def using[A <: Closeable](closeable: A): CallbackTask[A] =
    CallbackTask { f =>
      try f(closeable)
      finally closeable.close()
    }
}
