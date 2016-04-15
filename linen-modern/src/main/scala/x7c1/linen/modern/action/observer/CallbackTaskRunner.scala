package x7c1.linen.modern.action.observer

import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.patch.TaskAsync


object CallbackTaskRunner {
  def runAsync[A](onError: Throwable => Unit)(task: CallbackTask[A]) = {
    TaskAsync async {
      try {
        task.execute()
      } catch {
        case e: Exception => onError(e)
      }
    }
  }
}
