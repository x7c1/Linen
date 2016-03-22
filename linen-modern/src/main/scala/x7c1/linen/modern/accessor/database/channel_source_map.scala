package x7c1.linen.modern.accessor.database

import android.content.ContentValues
import x7c1.linen.modern.accessor.Insertable
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.database.TypedFields

trait channel_source_map extends TypedFields {
  def source_id: Long
  def channel_id: Long
  def created_at: Int --> Date
}
case class ChannelSourceMapParts(
  channelId: Long,
  sourceId: Long,
  createdAt: Date
)
object ChannelSourceMapParts {
  implicit object insertable extends Insertable[ChannelSourceMapParts] {
    override def tableName: String = "channel_source_map"
    override def toContentValues(target: ChannelSourceMapParts): ContentValues = {
      val column = TypedFields.expose[channel_source_map]
      TypedFields toContentValues (
        column.source_id -> target.sourceId,
        column.channel_id -> target.channelId,
        column.created_at -> target.createdAt
      )
    }
  }
}
