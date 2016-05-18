package x7c1.linen.repository.channel.subscribe

import x7c1.linen.database.mixin.SubscribedChannelRecord
import x7c1.linen.database.struct.{AccountIdentifiable, ChannelIdentifiable}
import x7c1.wheat.modern.database.selector.CursorConvertible
import x7c1.wheat.modern.database.selector.presets.{CanTraverseEntity, DefaultProvidable}
import x7c1.wheat.modern.features.HasShortLength

case class SubscribedChannel (
  channelId: Long,
  name: String
)

object SubscribedChannel {
  implicit object id extends ChannelIdentifiable[SubscribedChannel]{
    override def toId = _.channelId
  }
  implicit object convertible extends CursorConvertible[SubscribedChannelRecord, SubscribedChannel]{
    override def fromCursor = cursor =>
      SubscribedChannel(
        channelId = cursor.channel_id,
        name = cursor.name
      )
  }
  implicit object traversable extends CanTraverseEntity[
    AccountIdentifiable,
    SubscribedChannelRecord,
    SubscribedChannel ]

  implicit object providable extends DefaultProvidable[
    AccountIdentifiable,
    SubscribedChannel ]

  implicit object short extends HasShortLength[SubscribedChannel]
}
