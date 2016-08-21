package x7c1.linen.repository.inspector

import x7c1.linen.repository.inspector.LatentUrlError.UnknownFormat

object LatentUrl {
  private val pattern = """^(http[s]?://[^/]+)/.*""".r

  def create(originUrl: String, path: String): Either[LatentUrlError, LatentUrl] = {
    originUrl match {
      case pattern(base) =>
        Right(LatentUrlImpl(
          raw = path,
          full = if (path startsWith "/") base + path else path
        ))
      case n =>
        Left(UnknownFormat(path))
    }
  }
}

trait LatentUrl {

  def raw: String

  def full: String
}

case class LatentUrlImpl(
  override val raw: String,
  override val full: String) extends LatentUrl

sealed trait LatentUrlError {
  def message: String
}

object LatentUrlError {

  case class UnknownFormat(
    override val message: String) extends LatentUrlError

}
