package x7c1.linen.modern.struct

import android.database.Cursor
import x7c1.linen.modern.accessor.{ChannelRecordColumn, SingleSelectable}
import x7c1.wheat.macros.database.TypedCursor

case class Channel(
  channelId: Long,
  name: String,
  description: String,
  createdAt: Date
)

object Channel {
  implicit object singleSelectable extends SingleSelectable[Channel, Long]{

    override def tableName: String = "channels"

    override def where(id: Long) = Seq("_id" -> id.toString)

    override def fromCursor(rawCursor: Cursor) = {
      val cursor = TypedCursor[ChannelRecordColumn](rawCursor)
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
