package x7c1.linen.scene.inspector

import java.net.URL

import x7c1.linen.database.struct.HasSourceUrl
import x7c1.linen.repository.loader.queueing.UrlEnclosure


case class SourceActionUrl(
  actionId: Long,
  override val raw: URL) extends UrlEnclosure

object SourceActionUrl {

  implicit object url extends HasSourceUrl[SourceActionUrl] {
    override def toId = _.raw
  }

}
