package x7c1.linen.repository.loader.crawling

import x7c1.wheat.modern.fate.FateProvider.ErrorLike
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

trait SourceContentLoaderError {
  def detail: String
  def cause: Throwable
}

object SourceContentLoaderError {

  implicit object context extends ErrorLike[SourceContentLoaderError] {
    override def newInstance = UnexpectedError
  }

  case class UnexpectedError(e: Throwable) extends SourceContentLoaderError {
    override def detail = format(cause) {
      "[unexpected]"
    }
    override def cause: Throwable = e
  }

}
