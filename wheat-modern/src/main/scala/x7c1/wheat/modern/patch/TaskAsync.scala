package x7c1.wheat.modern.patch

import android.os.AsyncTask
import x7c1.wheat.modern.chrono.BufferingTimer

/**
 *
 * Substitute for AsyncTask,
 * which throws java.lang.AbstractMethodError when doInBackground is called.
 *
 */

trait TaskAsync[Params, Progress, Result] {

  def doInBackground(params: Params*): Result

  def onProgressUpdate(values: Progress*): Unit = {}

  def execute(params: Params*): AsyncTask[Params, Progress, Result] = {
    new AsyncTaskWrapper(this).execute(params:_*)
  }
}

object TaskAsync {
  def run[A](delay: Int)(f: => A): Unit = {
    val timer = new BufferingTimer(delay)
    val task = new TaskAsync[Unit, Unit, Unit] {
      override def doInBackground(params: Unit*): Unit = f
    }
    timer touch task.execute()
  }
  def async[A](f: => A): Unit = {
    val task = new TaskAsync[Unit, Unit, Unit] {
      override def doInBackground(params: Unit*): Unit = f
    }
    task.execute()
  }
}

private class AsyncTaskWrapper[Params, Progress, Result](
  task: TaskAsync[Params, Progress, Result]) extends AsyncTask[Params, Progress, Result]{

  override def doInBackground(params: Params*): Result = {
    task.doInBackground(params:_*)
  }

  override def onProgressUpdate(values: Progress*): Unit = {
    task.onProgressUpdate(values:_*)
  }
}

