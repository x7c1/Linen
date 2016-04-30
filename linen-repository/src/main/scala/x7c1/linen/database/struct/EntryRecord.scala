package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{EntityIdentifiable, UniqueSelectorFindable, Findable, Insertable, Query, SeqSelectable2}

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

  implicit object selectorFindable
    extends UniqueSelectorFindable[SourceIdentifiable, Seq[EntryRecord]]

  implicit object findable extends Findable[EntryRecord, Long]{
    override def reify(cursor: Cursor) = {
      TypedCursor[EntryRecord](cursor).freezeAt(0)
    }
    override def query(id: Long) = {
      val sql = "SELECT *, _id AS entry_id FROM entries WHERE _id = ?"
      new Query(sql, Array(id.toString))
    }
  }
  implicit object seq extends SeqSelectable2[SourceIdentifiable, EntryRecord]{
    override def reify(cursor: Cursor) = {
      TypedCursor[EntryRecord](cursor)
    }
    override def query[X: SourceIdentifiable](target: X): Query = {
      val sourceId = implicitly[SourceIdentifiable[X]] idOf target
      val sql = "SELECT *, _id AS entry_id FROM entries WHERE source_id = ?"
      new Query(sql, Array(sourceId.toString))
    }
  }
}

trait EntryIdentifiable[A] extends EntityIdentifiable[A, Long]

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
