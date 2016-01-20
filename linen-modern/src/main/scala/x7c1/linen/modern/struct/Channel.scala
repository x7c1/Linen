package x7c1.linen.modern.struct

import android.database.Cursor
import x7c1.linen.modern.accessor.SingleSelectable
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

    override def fromCursor(cursor: Cursor): Option[Channel] = {
      val idIndex = cursor getColumnIndex "_id"
      val nameIndex = cursor getColumnIndex "name"
      val descriptionIndex = cursor getColumnIndex "description"
      val createdAt = cursor getColumnIndex "created_at"
      if (cursor moveToPosition 0){
        Some apply Channel(
          channelId = cursor getLong idIndex,
          name = cursor getString nameIndex,
          description = cursor getString descriptionIndex,
          createdAt = Date(cursor getInt createdAt)
        )
      } else None
    }
    def fromCursorSample(rawCursor: Cursor) = {
      val cursor = TypedCursor[ChannelRecordColumn](rawCursor)
      if (cursor moveTo 0){
        Some apply Channel(
          channelId = cursor._id,
          description = cursor.description,
          name = cursor.name,
          createdAt = cursor.created_at.typed
        )
      } else None
    }
  }
}

trait ChannelRecordColumn extends TypedCursor {
  def _id: Long
  def name: String
  def description: String
  def created_at: Int --> Date
}
