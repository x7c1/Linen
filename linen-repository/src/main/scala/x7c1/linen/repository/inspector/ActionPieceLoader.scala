package x7c1.linen.repository.inspector

import java.net.UnknownHostException

import org.jsoup.Jsoup
import x7c1.wheat.macros.logger.Log

object ActionPieceLoader {

  import collection.JavaConverters._

  def loadFrom(pageUrl: String): Either[ActionPieceLoaderError, ActionPiece] = try {
    val document = Jsoup.connect(pageUrl).
      userAgent("").
      get()

    val links = document.select("""link[type^='']""")
    val eithers = links.asScala collect {
      case element if isSupportedType(element attr "type") =>
        LatentUrl.create(
          originUrl = pageUrl,
          path = element attr "href"
        )
    }
    val urls = eithers.collect {
      case Right(url) => url
    }
    eithers.collect {
      case Left(e) => Log error e.message
    }
    Right apply ActionPiece(
      originTitle = Option(document.title()) getOrElse "",
      originUrl = pageUrl,
      latentUrls = urls
    )
  } catch {
    case e: Exception =>
      Left apply ActionPieceLoaderError(
        targetUrl = pageUrl,
        cause = Some(e)
      )
  }

  private def isSupportedType(linkType: String): Boolean = {
    ActionPieceLoader.supportedTypes.getOrElse(linkType, false)
  }

  private val supportedTypes: Map[String, Boolean] = {
    val list = Seq(
      "application/rss+xml",
      "application/atom+xml"
    )
    list.map(_ -> true).toMap
  }

}

case class ActionPiece(
  originTitle: String,
  originUrl: String,
  latentUrls: Seq[LatentUrl]
)

case class ActionPieceLoaderError(
  targetUrl: String,
  cause: Option[Exception]
)
