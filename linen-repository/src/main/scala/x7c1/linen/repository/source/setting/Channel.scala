package x7c1.linen.repository.source.setting

import x7c1.linen.database.struct.{ChannelIdentifiable, ChannelRecord}
import x7c1.linen.repository.date.Date
import x7c1.wheat.modern.database.selector.CursorConvertible
import x7c1.wheat.modern.database.selector.presets.{CanFindEntity, DefaultProvidable}

case class Channel(
  channelId: Long,
  name: String,
  description: String,
  createdAt: Date
)

object Channel {
  def table = "channels"

  implicit object providable extends DefaultProvidable[ChannelIdentifiable, Channel]

  implicit object convertible extends CursorConvertible[ChannelRecord, Channel]{
    override def fromCursor = cursor =>
      Channel(
        channelId = cursor._id,
        description = cursor.description,
        name = cursor.name,
        createdAt = cursor.created_at.typed
      )
  }
  implicit object findable extends CanFindEntity[ChannelIdentifiable, ChannelRecord, Channel]
}
