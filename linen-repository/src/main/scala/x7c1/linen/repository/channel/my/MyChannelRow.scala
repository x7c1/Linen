package x7c1.linen.repository.channel.my

import x7c1.linen.database.mixin.MyChannelRecord
import x7c1.linen.database.struct.{AccountIdentifiable, ChannelDeletable, ChannelIdentifiable}
import x7c1.linen.repository.date.Date
import x7c1.wheat.modern.database.selector.CursorConvertible
import x7c1.wheat.modern.database.selector.presets.{DefaultProvidable, CanTraverseEntity}

sealed trait MyChannelRow

case class MyChannel(
  channelId: Long,
  name: String,
  description: String,
  createdAt: Date,
  isSubscribed: Boolean ) extends MyChannelRow

object MyChannel {
  implicit object id extends ChannelIdentifiable[MyChannel]{
    override def toId = _.channelId
  }
  implicit object deletable extends ChannelDeletable[MyChannel](_.channelId)

  implicit object convertible extends CursorConvertible[MyChannelRecord, MyChannel]{
    override def fromCursor = cursor =>
      MyChannel(
        channelId = cursor._id,
        name = cursor.name,
        description = cursor.description,
        createdAt = cursor.created_at.typed,
        isSubscribed = cursor.subscribed == 1
      )
  }
  implicit object traversable extends CanTraverseEntity[AccountIdentifiable, MyChannelRecord, MyChannel]

  implicit object providable extends DefaultProvidable[AccountIdentifiable, MyChannel]
}

case class MyChannelFooter() extends MyChannelRow