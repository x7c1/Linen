package x7c1.linen.repository.loader.crawling

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

trait SourceQueueError {
  def detail: String
  def cause: Throwable
}

object SourceQueueError {
  implicit object unknown extends HasConstructor[Throwable => SourceQueueError]{
    override def newInstance = UnknownError(_)
  }
  case class LoadingError(original: SourceLoaderError) extends SourceQueueError {
    def cause = original.cause
    def detail = format(cause){"[failed] loading error"}
  }
  case class UnknownError(cause: Throwable) extends SourceQueueError {
    override def detail = format(cause){"[failed] unknown error"}
  }
}
