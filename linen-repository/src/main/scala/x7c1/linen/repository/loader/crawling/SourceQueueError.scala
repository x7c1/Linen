package x7c1.linen.repository.loader.crawling

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

trait SourceQueueError {
  def detail: String

  def cause: Option[Throwable]

  def toThrowable: Throwable = cause match {
    case Some(e) => e
    case None => new Exception(detail)
  }
}

object SourceQueueError {

  implicit object unknown extends HasConstructor[Throwable => SourceQueueError] {
    override def newInstance = UnknownError
  }

  case class LoadingError(original: SourceLoaderError) extends SourceQueueError {
    def cause = original.cause

    def detail = original.detail
  }

  case class UnknownError(origin: Throwable) extends SourceQueueError {
    override def detail = format(origin) {
      "[failed] unknown error"
    }

    override def cause = Some(origin)
  }

}
