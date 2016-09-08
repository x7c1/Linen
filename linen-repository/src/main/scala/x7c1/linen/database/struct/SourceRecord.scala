package x7c1.linen.database.struct

import java.net.URL

import android.content.ContentValues
import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{Query, TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{HasTable, Insertable}
import x7c1.wheat.modern.database.selector.presets.{CanFindBySelect, CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}

import scala.language.higherKinds

object SourceRecord {

  def table: String = "sources"

  def column = TypedFields.expose[SourceRecord]

  implicit object hasTable extends HasTable.Where[SourceRecord](table)

  implicit object reifiable extends RecordReifiable[SourceRecord] {
    override def reify(cursor: Cursor) = TypedCursor[SourceRecord](cursor)
  }

  implicit object providable
    extends DefaultProvidable[HasSourceId, SourceRecord]

  implicit object urlFindable extends CanFindRecord.Where[HasSourceUrl, SourceRecord] {
    override def where[X](url: URL) = toArgs(column.url -> url.toExternalForm)
  }

  implicit object idFindable extends CanFindRecord.Where[HasSourceId, SourceRecord] {
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

trait HasSourceId[A] extends Identifiable[A, Long]

trait HasSourceUrl[A] extends Identifiable[A, URL]

object HasSourceId {

  implicit object id extends HasSourceId[Long] with IdEndo[Long]

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
      TypedFields toContentValues(
        column.title -> target.title,
        column.url -> target.url,
        column.description -> target.description,
        column.created_at -> target.createdAt
        )
    }
  }

}
