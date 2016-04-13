package x7c1.linen.database

import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields

trait ChannelRecord extends TypedFields {
  def _id: Long
  def name: String
  def description: String
  def account_id: Long
  def created_at: Int --> Date
}

object ChannelRecord {
  def table: String = "channels"
  def column: ChannelRecord = TypedFields.expose[ChannelRecord]
}

case class ChannelParts(
  accountId: Long,
  name: String,
  description: String,
  createdAt: Date
)

object ChannelParts {
  import ChannelRecord.column

  implicit object insertable extends Insertable[ChannelParts] {

    override def tableName = ChannelRecord.table

    override def toContentValues(parts: ChannelParts) = {
      TypedFields toContentValues (
        column.name -> parts.name,
        column.description -> parts.description,
        column.account_id -> parts.accountId,
        column.created_at -> parts.createdAt
      )
    }
  }
}
