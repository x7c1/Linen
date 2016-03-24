package x7c1.linen.modern.accessor.unread

case class UnreadChannel(
  channelId: Long,
  name: String
)

object UnreadChannel {
  implicit object selectable extends ChannelSelectable[UnreadChannel] {
    override def channelIdOf = _.channelId
    override def nameOf = _.name
  }
}