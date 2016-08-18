package x7c1.linen.repository.inspector

import org.jsoup.Jsoup
import x7c1.linen.database.struct.HasAccountId


object ActionPieceLoader {
  def apply[A: HasAccountId](account: A): ActionPieceLoader[A] = {
    new ActionPieceLoader(account)
  }

  private val supportedTypes: Map[String, Boolean] = {
    val list = Seq(
      "application/atom+xml"
    )
    list.map(_ -> true).toMap
  }
}

class ActionPieceLoader[A: HasAccountId] private(account: A) {

  import collection.JavaConverters._

  def loadFrom(pageUrl: String): ActionPiece = {
    val document = Jsoup.connect(pageUrl).get()
    val urls = document.select("""link[type^='']""").asScala.
      collect {
        case element if isSupportedType(element attr "type") =>
          element attr "href"
      }

    ActionPiece(
      accountId = implicitly[HasAccountId[A]] toId account,
      originTitle = Option(document.title()) getOrElse "",
      originUrl = pageUrl,
      latentUrls = urls
    )
  }
  def isSupportedType(linkType: String): Boolean = {
    ActionPieceLoader.supportedTypes.getOrElse(linkType, false)
  }
}

case class ActionPiece(
  accountId: Long,
  originTitle: String,
  originUrl: String,
  latentUrls: Seq[String]
)
