package x7c1.linen.repository.channel.preset

import x7c1.linen.database.struct.{ChannelRecord, NamedChannelIdentifiable}
import x7c1.wheat.modern.database.selector.CursorConvertible
import x7c1.wheat.modern.database.selector.presets.{CanFindEntity, DefaultProvidable}

case class PresetChannel(
  channelId: Long,
  accountId: Long,
  name: String
)
object PresetChannel {
  implicit object convertible extends CursorConvertible[ChannelRecord, PresetChannel]{
    override def fromCursor = cursor =>
      PresetChannel(
        channelId = cursor._id,
        accountId = cursor.account_id,
        name = cursor.name
      )
  }
  implicit object findable extends CanFindEntity[NamedChannelIdentifiable, ChannelRecord, PresetChannel]

  implicit object providable extends DefaultProvidable[NamedChannelIdentifiable, PresetChannel]
}

case class PresetChannelPiece(
  name: String,
  description: String
)
