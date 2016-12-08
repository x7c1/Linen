package x7c1.linen.modern.init.inspector

import x7c1.linen.database.struct.HasSourceId

object SubscribeClickedEvent {

  implicit object source extends HasSourceId[SubscribeClickedEvent] {
    override def toId = _.sourceId
  }

}

case class SubscribeClickedEvent(
  sourceId: Long
)
