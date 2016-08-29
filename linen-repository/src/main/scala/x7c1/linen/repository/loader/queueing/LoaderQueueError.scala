package x7c1.linen.repository.loader.queueing

import x7c1.wheat.modern.fate.FateProvider.ErrorLike
import x7c1.wheat.modern.queue.map.TrackableQueue.CanThrow

sealed trait LoaderQueueError {
  def message: String
}

class LoaderQueueException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = {
    this(message, null)
  }
}

object LoaderQueueError {

  case class Unexpected(cause: Throwable) extends LoaderQueueError {
    override def message = cause.getMessage
  }

  implicit object canThrow extends CanThrow[LoaderQueueError] {
    override def asThrowable(error: LoaderQueueError) = {
      error match {
        case Unexpected(cause) => new LoaderQueueException(error.message, cause)
        case e => new LoaderQueueException(e.message)
      }
    }
  }

  implicit object errorLike extends ErrorLike[LoaderQueueError] {
    override def newInstance = Unexpected
  }

}
