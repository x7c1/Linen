package x7c1.linen.repository.loader.crawling

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

trait SourceLoaderError extends Exception {
  def cause: Throwable

  def detail: String
}

object SourceLoaderError {

  implicit object unknown extends HasConstructor[Throwable => SourceLoaderError] {
    override def newInstance = UnknownError
  }

  case class Wrapped(
    override val cause: Throwable,
    override val detail: String ) extends SourceLoaderError

  case class UnknownError(cause: Throwable) extends SourceLoaderError {
    override def detail = format(cause) {
      "[failed] unknown error"
    }
  }

}
