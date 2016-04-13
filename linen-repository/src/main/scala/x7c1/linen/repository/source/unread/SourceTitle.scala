package x7c1.linen.repository.source.unread

import android.content.ContentValues
import android.database.Cursor
import x7c1.linen.database.struct.SourceRecord
import x7c1.linen.database.struct.SourceRecord.table
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{SingleWhere, Updatable}

case class SourceTitle(
  sourceId: Long,
  title: String
)

object SourceTitle {
  import SourceRecord.column

  implicit object updatable extends Updatable[SourceTitle]{
    override def tableName = table
    override def toContentValues(target: SourceTitle): ContentValues = {
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
      val cursor = TypedCursor[SourceRecord](rawCursor)
      cursor.moveToFind(0){
        SourceTitle(cursor._id, cursor.title)
      }
    }
  }
}
