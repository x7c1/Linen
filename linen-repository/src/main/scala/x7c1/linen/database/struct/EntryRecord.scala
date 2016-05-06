package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Insertable
import x7c1.wheat.modern.database.selector.presets.{CanCollectRecord, CanFindRecord, CollectFrom, FindBy}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable, SelectorProvidable}

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

  implicit object reifiable extends RecordReifiable[EntryRecord]{
    override def reify(cursor: Cursor) = TypedCursor[EntryRecord](cursor)
  }
  implicit object providable
    extends SelectorProvidable[EntryRecord, Selector](new Selector(_))

  implicit object findable extends CanFindRecord.Where[EntryIdentifiable, EntryRecord](table){
    override def where[X](id: Long) = Seq("entry_id" -> id.toString)
  }
  implicit object collectable extends CanCollectRecord.Where[SourceIdentifiable, EntryRecord](table){
    override def where[X](id: Long) = Seq("source_id" -> id.toString)
  }
  class Selector(val db: SQLiteDatabase)
    extends CollectFrom[SourceIdentifiable, EntryRecord]
      with FindBy[EntryIdentifiable, EntryRecord]
}

trait EntryIdentifiable[A] extends Identifiable[A, Long]

object EntryIdentifiable {
  implicit object entryIdentifiable extends EntryIdentifiable[Long] with IdEndo[Long]
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
