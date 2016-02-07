package x7c1.wheat.modern.callback

import java.io.Closeable

import x7c1.wheat.modern.kinds.CallbackBase

import scala.concurrent.{Promise, Future}

class CallbackTask[EVENT](
  callback: (EVENT => Unit) => Unit) extends CallbackBase[EVENT] {

  override type This[A] = CallbackTask[A]

  override def apply(f: EVENT => Unit): Unit = callback(f)

  def execute(): Unit = callback(_ => ())

  def toFuture: Future[EVENT] = {
    val promise = Promise[EVENT]()
    callback { event =>
      try promise trySuccess event
      catch { case e: Throwable => promise tryFailure e }
    }
    promise.future
  }
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
