package x7c1.linen.repository.loader.crawling

import x7c1.wheat.modern.fate.FateProvider.ErrorLike
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

trait SourceContentLoaderError {
  def detail: String
  def cause: Option[Throwable]
}

object SourceContentLoaderError {

  implicit object context extends ErrorLike[SourceContentLoaderError] {
    override def newInstance = UnexpectedError
  }

  case class LoaderParseError(
    override val detail: String,
    override val cause: Option[Throwable]) extends SourceContentLoaderError

  case class UnexpectedError(origin: Throwable) extends SourceContentLoaderError {
    override def detail = format(origin) {
      "[unexpected]"
    }

    override def cause: Option[Throwable] = Some(origin)
  }

}
