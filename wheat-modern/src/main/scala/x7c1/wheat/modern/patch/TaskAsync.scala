package x7c1.wheat.modern.patch

import android.os.AsyncTask

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

private class AsyncTaskWrapper[Params, Progress, Result](
  task: TaskAsync[Params, Progress, Result]) extends AsyncTask[Params, Progress, Result]{

  override def doInBackground(params: Params*): Result = {
    task.doInBackground(params:_*)
  }

  override def onProgressUpdate(values: Progress*): Unit = {
    task.onProgressUpdate(values:_*)
  }
}
