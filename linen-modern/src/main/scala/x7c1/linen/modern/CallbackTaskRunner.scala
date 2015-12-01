package x7c1.linen.modern

import x7c1.wheat.modern.callback.CallbackTask

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task


object CallbackTaskRunner {
  def runAsync[A](onError: Throwable => Unit)(task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => onError(e)
    }
  }
}
