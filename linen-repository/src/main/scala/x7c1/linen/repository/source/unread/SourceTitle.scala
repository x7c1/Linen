package x7c1.linen.repository.source.unread

import android.content.ContentValues
import x7c1.linen.database.struct.SourceRecord.table
import x7c1.linen.database.struct.{SourceIdentifiable, SourceRecord}
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.modern.database.Updatable
import x7c1.wheat.modern.database.selector.CursorConvertible
import x7c1.wheat.modern.database.selector.presets.{CanFindEntity, DefaultProvidable}

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
  implicit object providable extends DefaultProvidable[SourceIdentifiable, SourceTitle]

  implicit object convertible extends CursorConvertible[SourceRecord, SourceTitle]{
    override def fromCursor = cursor =>
      SourceTitle(
        sourceId = cursor._id,
        title = cursor.title
      )
  }
  implicit object findable extends CanFindEntity[SourceIdentifiable, SourceRecord, SourceTitle]
}
