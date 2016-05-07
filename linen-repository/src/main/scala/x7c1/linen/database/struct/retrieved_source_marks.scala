package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Insertable
import x7c1.wheat.modern.database.selector.RecordReifiable
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}

object retrieved_source_marks {
  val table = "retrieved_source_marks"
  val column = TypedFields.expose[retrieved_source_marks]

  implicit object providable
    extends DefaultProvidable[SourceIdentifiable, retrieved_source_marks]

  implicit object reifiable extends RecordReifiable[retrieved_source_marks]{
    override def reify(cursor: Cursor) = TypedCursor[retrieved_source_marks](cursor)
  }
  implicit object findable extends CanFindRecord.Where[SourceIdentifiable, retrieved_source_marks](table){
    override def where[X](id: Long) = toArgs(column.source_id -> id)
  }
  implicit object entryId extends EntryIdentifiable[retrieved_source_marks]{
    override def toId = _.latest_entry_id
  }
}

trait retrieved_source_marks extends TypedFields {
  def source_id: Long
  def latest_entry_id: Long
  def latest_entry_created_at: Int --> Date
  def updated_at: Int --> Date
}

case class RetrievedSourceMarkParts(
  sourceId: Long,
  latestEntryId: Long,
  latestEntryCreatedAt: Date,
  updatedAt: Date
)

object RetrievedSourceMarkParts {
  import retrieved_source_marks.column

  implicit object insertable extends Insertable[RetrievedSourceMarkParts]{
    override def tableName: String = retrieved_source_marks.table
    override def toContentValues(target: RetrievedSourceMarkParts) =
      TypedFields toContentValues (
        column.source_id -> target.sourceId,
        column.latest_entry_id -> target.latestEntryId,
        column.latest_entry_created_at -> target.latestEntryCreatedAt,
        column.updated_at -> target.updatedAt
      )
  }
}
