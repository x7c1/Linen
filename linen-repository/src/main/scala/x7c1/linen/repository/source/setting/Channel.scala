package x7c1.linen.repository.source.setting

import android.database.Cursor
import x7c1.linen.database.{ChannelRecord, SingleWhere}
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedCursor

case class Channel(
  channelId: Long,
  name: String,
  description: String,
  createdAt: Date
)

object Channel {
  def table = "channels"

  implicit object singleSelectable extends SingleWhere[Channel, Long](table){
    override def where(id: Long) = Seq("_id" -> id.toString)
    override def fromCursor(rawCursor: Cursor) = {
      val cursor = TypedCursor[ChannelRecord](rawCursor)
      cursor.moveToFind(0){
        Channel(
          channelId = cursor._id,
          description = cursor.description,
          name = cursor.name,
          createdAt = cursor.created_at.typed
        )
      }
    }
  }
}
