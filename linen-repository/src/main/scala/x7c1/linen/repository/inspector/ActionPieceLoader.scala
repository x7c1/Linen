package x7c1.linen.repository.inspector

import org.jsoup.Jsoup
import x7c1.wheat.macros.logger.Log

object ActionPieceLoader {

  import collection.JavaConverters._

  def loadFrom(pageUrl: String): ActionPiece = {
    Log info s"[init] $pageUrl"

    val document = Jsoup.connect(pageUrl).get()
    val eithers = document.select("""link[type^='']""").asScala.
      collect {
        case element if isSupportedType(element attr "type") =>
          LatentUrl.create(
            originUrl = pageUrl,
            path = element attr "href"
          )
      }

    val urls = eithers.collect {
      case Right(url) => url
    }

    urls foreach { Log info _.full }

    eithers.collect {
      case Left(e) => Log error e.message
    }
    ActionPiece(
      originTitle = Option(document.title()) getOrElse "",
      originUrl = pageUrl,
      latentUrls = urls
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
