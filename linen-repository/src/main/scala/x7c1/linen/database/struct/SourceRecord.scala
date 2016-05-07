package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Insertable
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}

import scala.language.higherKinds

object SourceRecord {

  def table: String = "sources"

  def column = TypedFields.expose[SourceRecord]

  implicit object reifiable extends RecordReifiable[SourceRecord]{
    override def reify(cursor: Cursor) = TypedCursor[SourceRecord](cursor)
  }
  implicit object providable
    extends DefaultProvidable[SourceIdentifiable, SourceRecord]

  implicit object findable extends CanFindRecord.Where[SourceIdentifiable, SourceRecord](table){
    override def where[X](id: Long) = toArgs(column._id -> id)
  }
}

trait SourceRecord extends TypedFields {
  def _id: Long
  def title: String
  def description: String
  def url: String
  def created_at: Int --> Date
}

trait SourceIdentifiable[A] extends Identifiable[A, Long]

object SourceIdentifiable {
  implicit object id extends SourceIdentifiable[Long] with IdEndo[Long]
}

case class SourceParts(
  title: String,
  url: String,
  description: String,
  createdAt: Date
)

object SourceParts {
  implicit object insertable extends Insertable[SourceParts] {
    override def tableName = SourceRecord.table
    override def toContentValues(target: SourceParts): ContentValues = {
      val column = TypedFields.expose[SourceRecord]
      TypedFields toContentValues (
        column.title -> target.title,
        column.url -> target.url,
        column.description -> target.description,
        column.created_at -> target.createdAt
      )
    }
  }
}
