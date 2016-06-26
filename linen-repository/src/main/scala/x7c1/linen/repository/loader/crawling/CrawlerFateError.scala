package x7c1.linen.repository.loader.crawling

import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

sealed trait CrawlerFateError {
  def cause: Throwable
  def detail: String
}

object CrawlerFateError {
  implicit object unknown extends HasConstructor[Throwable => CrawlerFateError]{
    override def newInstance = UnknownError(_)
  }
  case class UnknownError(cause: Throwable) extends CrawlerFateError {
    override def detail = format(cause){"[failed] unknown error"}
  }
}
