package x7c1.linen.repository.channel.unread

import x7c1.linen.repository.channel.unread.selector.{CanTraverseImpl, UnreadChannelSelector}
import x7c1.wheat.modern.database.selector.SelectorProvidable
import x7c1.wheat.modern.features.HasShortLength

case class UnreadChannel(
  channelId: Long,
  name: String
)

object UnreadChannel {
  implicit object short extends HasShortLength[UnreadChannel]

  implicit object selectable extends ChannelSelectable[UnreadChannel] {
    override def toId = _.channelId
    override def nameOf = _.name
  }
  implicit object providable
    extends SelectorProvidable[UnreadChannel, UnreadChannelSelector](new UnreadChannelSelector(_))

  implicit object traverse extends CanTraverseImpl
}
