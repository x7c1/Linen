package x7c1.linen.repository.inspector

import java.net.{MalformedURLException, URL}

import x7c1.linen.repository.inspector.ActionPageUrlError.{EmptyUrl, InvalidFormat, Unexpected}
import x7c1.linen.repository.loader.queueing.UrlEnclosure
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

case class ActionPageUrl(
  accountId: Long,
  override val raw: URL) extends UrlEnclosure

object ActionPageUrl {
  def create(accountId: Long, url: String): Either[ActionPageUrlError, ActionPageUrl] = {
    try {
      if (Option(url).getOrElse("").isEmpty) {
        Left apply EmptyUrl()
      } else {
        Right apply ActionPageUrl(
          accountId = accountId,
          raw = new URL(url)
        )
      }
    } catch {
      case e: MalformedURLException =>
        Left apply InvalidFormat(format(e)("[malformed]"), Some(e))
      case e: Exception =>
        Left apply Unexpected(format(e)("[unexpected]"), Some(e))
    }
  }
}

trait ActionPageUrlError {

  def detail: String

  def cause: Option[Throwable]
}

object ActionPageUrlError {

  case class EmptyUrl() extends ActionPageUrlError {
    override val detail: String = "URL required"
    override val cause: Option[Throwable] = None
  }

  case class InvalidFormat(
    override val detail: String,
    override val cause: Option[Throwable]) extends ActionPageUrlError {
  }

  case class Unexpected(
    override val detail: String,
    override val cause: Option[Throwable]) extends ActionPageUrlError {
  }

}
