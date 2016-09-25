package x7c1.linen.scene.inspector

import x7c1.linen.database.struct.HasAccountId

case class PageActionStartedEvent(accountId: Long)

object PageActionStartedEvent {
  implicit object account extends HasAccountId[PageActionStartedEvent]{
    override def toId = _.accountId
  }
}
