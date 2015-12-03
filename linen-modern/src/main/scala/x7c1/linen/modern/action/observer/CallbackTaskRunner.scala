package x7c1.linen.modern.action.observer

import x7c1.wheat.modern.callback.CallbackTask

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}


object CallbackTaskRunner {
  def runAsync[A](onError: Throwable => Unit)(task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => onError(e)
    }
  }
}
