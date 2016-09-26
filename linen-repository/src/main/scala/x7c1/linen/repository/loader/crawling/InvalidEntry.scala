package x7c1.linen.repository.loader.crawling

import java.net.URL

import x7c1.wheat.modern.formatter.ThrowableFormatter.format

sealed trait InvalidEntry {
  def detail: String
}

case class EmptyUrl(sourceUrl: URL) extends InvalidEntry {
  override def detail = s"entry with no url found in $sourceUrl"
}

case class EmptyPublishedDate(entryUrl: URL) extends InvalidEntry {
  override def detail = s"published-date not found in entry:$entryUrl"
}

case class Abort[A <: Throwable](cause: A) extends InvalidEntry {
  override def detail = format(cause)("[aborted]")
}
