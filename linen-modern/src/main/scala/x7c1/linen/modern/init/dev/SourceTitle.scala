package x7c1.linen.modern.init.dev

import android.content.ContentValues
import android.database.Cursor
import x7c1.linen.modern.accessor.SourceRecordColumn.table
import x7c1.linen.modern.accessor.{SingleWhere, SourceRecordColumn, Updatable}
import x7c1.wheat.macros.database.{TypedFields, TypedCursor}

case class SourceTitle(
  sourceId: Long,
  title: String
)

object SourceTitle {
  implicit object updatable extends Updatable[SourceTitle]{
    override def tableName: String = "sources"
    override def toContentValues(target: SourceTitle): ContentValues = {
      val column = TypedFields.expose[SourceRecordColumn]
      TypedFields toContentValues (
        column._id -> target.sourceId,
        column.title -> target.title
      )
    }
    override def where(target: SourceTitle): Seq[(String, String)] = Seq(
      "_id" -> target.sourceId.toString
    )
  }
  implicit object selectable extends SingleWhere[SourceTitle, Long](table){
    override def where(id: Long) = Seq("_id" -> id.toString)
    override def fromCursor(rawCursor: Cursor) = {
      val cursor = TypedCursor[SourceRecordColumn](rawCursor)
      cursor.moveToFind(0){
        SourceTitle(cursor._id, cursor.title)
      }
    }
  }
}
