package x7c1.linen.repository.loader.queueing

import x7c1.wheat.modern.fate.FateProvider.ErrorLike
import x7c1.wheat.modern.queue.map.TrackableQueue.CanThrow

sealed trait UrlTraverserError {
  def message: String
}

class LoaderQueueException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = {
    this(message, null)
  }
}

object UrlTraverserError {

  case class Unexpected(cause: Throwable) extends UrlTraverserError {
    override def message = cause.getMessage
  }

  implicit object canThrow extends CanThrow[UrlTraverserError] {
    override def asThrowable(error: UrlTraverserError) = {
      error match {
        case Unexpected(cause) => new LoaderQueueException(error.message, cause)
        case e => new LoaderQueueException(e.message)
      }
    }
  }

  implicit object errorLike extends ErrorLike[UrlTraverserError] {
    override def newInstance = Unexpected
  }

}
