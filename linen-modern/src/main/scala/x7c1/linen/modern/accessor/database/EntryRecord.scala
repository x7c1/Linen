package x7c1.linen.modern.accessor.database

import android.content.ContentValues
import x7c1.linen.modern.accessor.{Insertable, EntryUrl}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.database.TypedFields

trait EntryRecord extends TypedFields {
  def entry_id: Long
  def source_id: Long
  def title: String
  def content: String
  def url: String
  def created_at: Int --> Date
}
object EntryRecord {
  def table: String = "entries"
}

case class EntryParts(
  sourceId: Long,
  title: String,
  content: String,
  url: EntryUrl,
  createdAt: Date
)
object EntryParts {
  implicit object insertable extends Insertable[EntryParts] {
    override def tableName: String = EntryRecord.table
    override def toContentValues(target: EntryParts): ContentValues = {
      val column = TypedFields.expose[EntryRecord]
      TypedFields toContentValues (
        column.source_id -> target.sourceId,
        column.title -> target.title,
        column.content -> target.content,
        column.url -> target.url.raw,
        column.created_at -> target.createdAt
        )
    }
  }
}
