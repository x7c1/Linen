package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{Findable, Insertable, Query, SeqSelectable}

trait EntryRecord extends TypedFields {
  def entry_id: Long
  def source_id: Long
  def title: String
  def content: String
  def author: String
  def url: String
  def created_at: Int --> Date
}
object EntryRecord {
  def table: String = "entries"

  implicit object selectable extends SingleSelectable[EntryRecord, Long]{
    override def query(id: Long): Query = {
      val sql = "SELECT *, _id AS entry_id FROM entries WHERE _id = ?"
      new Query(sql, Array(id.toString))
    }
    override def fromCursor(cursor: Cursor): Option[EntryRecord] =
      TypedCursor[EntryRecord](cursor).freezeAt(0)
  }
}

case class EntryParts(
  sourceId: Long,
  title: String,
  content: String,
  author: String,
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
        column.author -> target.author,
        column.url -> target.url.raw,
        column.created_at -> target.createdAt
      )
    }
  }
}
