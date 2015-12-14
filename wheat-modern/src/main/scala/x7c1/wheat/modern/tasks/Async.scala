package x7c1.wheat.modern.tasks

import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.patch.TaskAsync.after

object Async {
  def await(msec: Long): CallbackTask[Unit] = {
    CallbackTask(f => after(msec){ f({}) })
  }
}
