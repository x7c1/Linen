package x7c1.linen.modern.init.settings.order

import x7c1.linen.database.struct.HasAccountId

case class ChannelOrdered(
  accountId: Long
)
object ChannelOrdered {
  implicit object account extends HasAccountId[ChannelOrdered]{
    override def toId = _.accountId
  }
}
