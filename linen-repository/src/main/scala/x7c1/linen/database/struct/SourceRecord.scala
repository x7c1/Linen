package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{SelectorFindable, Insertable, MultipleSelectable, ReadableDatabase, SingleWhere}

import scala.language.higherKinds

object SourceRecord {

  def table: String = "sources"

  def column = TypedFields.expose[SourceRecord]

  implicit object selectable extends SingleWhere[SourceRecord, Long](table){
    override def where(id: Long): Seq[(String, String)] = Seq(
      "_id" -> id.toString
    )
    override def fromCursor(rawCursor: Cursor): Option[SourceRecord] = {
      TypedCursor[SourceRecord](rawCursor) freezeAt 0
    }
  }
}

trait SourceRecord extends TypedFields {
  def _id: Long
  def title: String
  def description: String
  def url: String
  def created_at: Int --> Date
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
