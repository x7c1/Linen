package x7c1.linen.repository.loader.crawling

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

trait SourceLoaderError extends Exception {
  def cause: Option[Throwable]

  def detail: String
}

object SourceLoaderError {

  implicit object unknown extends HasConstructor[Throwable => SourceLoaderError] {
    override def newInstance = UnknownError
  }

  case class Affected(
    override val cause: Option[Throwable],
    override val detail: String) extends SourceLoaderError

  case class UnknownError(origin: Throwable) extends SourceLoaderError {
    override def detail = format(origin) {
      "[failed] unknown error"
    }

    override def cause = Some(origin)
  }

}
