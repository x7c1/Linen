package x7c1.linen.modern.init.dev

import android.content.ContentValues
import x7c1.linen.modern.accessor.{SourceRecordColumn, Updatable}
import x7c1.wheat.macros.database.TypedCursor

case class SourceTitle(
  sourceId: Long,
  title: String
)

object SourceTitle {
  implicit object updatable extends Updatable[SourceTitle]{
    override def tableName: String = "sources"
    override def toContentValues(target: SourceTitle): ContentValues = {
      val column = TypedCursor.expose[SourceRecordColumn]
      TypedCursor.toContentValues(
        column._id -> target.sourceId,
        column.title -> target.title
      )
    }
    override def where(target: SourceTitle): Seq[(String, String)] = Seq(
      "_id" -> target.sourceId.toString
    )
  }
}
