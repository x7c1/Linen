package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{HasTable, Insertable}
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

  def column = TypedFields.expose[EntryRecord]

  implicit object hasTable extends HasTable.Where[EntryRecord](table)

  implicit object reifiable extends RecordReifiable[EntryRecord]{
    override def reify(cursor: Cursor) = TypedCursor[EntryRecord](cursor)
  }
  implicit object providable extends SelectorProvidable[EntryRecord, Selector]

  implicit object findable extends CanFindRecord.Where[HasEntryId, EntryRecord]{
    override def where[X](id: Long) = toArgs(column.entry_id -> id)
  }
  implicit object collectable extends CanCollectRecord.Where[HasSourceId, EntryRecord]{
    override def where[X](id: Long) = toArgs(column.source_id -> id)
  }
  class Selector(val db: SQLiteDatabase)
    extends CollectFrom[HasSourceId, EntryRecord]
      with FindBy[HasEntryId, EntryRecord]
}

trait HasEntryId[A] extends Identifiable[A, Long]

object HasEntryId {
  implicit object entryId extends HasEntryId[Long] with IdEndo[Long]
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
