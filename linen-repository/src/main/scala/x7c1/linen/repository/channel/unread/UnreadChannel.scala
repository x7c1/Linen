package x7c1.linen.repository.channel.unread

case class UnreadChannel(
  channelId: Long,
  name: String
)

object UnreadChannel {
  implicit object selectable extends ChannelSelectable[UnreadChannel] {
    override def toId = _.channelId
    override def nameOf = _.name
  }
}
