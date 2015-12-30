package x7c1.wheat.modern.patch

import android.app.LoaderManager
import android.app.LoaderManager.LoaderCallbacks
import android.content.{Context, AsyncTaskLoader, Loader}
import android.os.Bundle

import scala.concurrent.{Promise, Future}

class FiniteLoaderFactory(
  context: Context, loaderManager: LoaderManager, startLoaderId: Int) {

  private var currentLoaderId: Option[Int] = None

  def close(): Unit = {
    currentLoaderId = None
  }

  def asFuture[A](f: => A): Future[A] = {
    val loaderId = nextLoaderId()
    val promise = Promise[A]()
    loaderManager.restartLoader(loaderId, new Bundle(), loaderCallbacks(promise, f))
    promise.future
  }

  private def nextLoaderId() = synchronized {
    val current = currentLoaderId getOrElse (startLoaderId - 1)
    val next = current + 1
    currentLoaderId = Some(next)
    next
  }

  private def createLoader[A](promise: Promise[A], f: => A) =
    new AsyncTaskLoader[Unit](context) {
      override def loadInBackground(): Unit = {
        try promise trySuccess f
        catch { case e: Throwable => promise tryFailure e }
      }
      override def onStartLoading(): Unit = forceLoad()
    }

  private def loaderCallbacks[A](promise: Promise[A], f: => A) =
    new LoaderCallbacks[Unit] {
      override def onCreateLoader(id: Int, args: Bundle) = createLoader(promise, f)
      override def onLoaderReset(loader: Loader[Unit]) = {}
      override def onLoadFinished(loader: Loader[Unit], data: Unit) = {}
    }
}
