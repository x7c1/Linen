package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.Query._
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Insertable
import x7c1.wheat.modern.database.selector.presets.{CanTraverseRecordByQuery, TraverseAll}
import x7c1.wheat.modern.database.selector.{RecordReifiable, SelectorProvidable}

trait InspectorSourceRecord extends TypedFields {
  def action_id: Long

  def source_loading_status: Int --> InspectorLoadingStatus

  def latent_source_url: String

  def discovered_source_id: Option[Long]

  def created_at: Int --> Date

  def updated_at: Int --> Date
}

object InspectorSourceRecord {
  def table = "inspector_sources"

  def column = TypedFields.expose[InspectorSourceRecord]

  implicit object providable
    extends SelectorProvidable[InspectorSourceRecord, Selector]

  implicit object reifiable extends RecordReifiable[InspectorSourceRecord] {
    override def reify(cursor: Cursor) = TypedCursor[InspectorSourceRecord](cursor)
  }

  implicit object traverseAll extends CanTraverseRecordByQuery[InspectorSourceRecord](
    sql"SELECT * FROM inspector_sources"
  )

  class Selector(protected val db: SQLiteDatabase)
    extends TraverseAll[InspectorSourceRecord]

}

case class InspectorSourceParts(
  actionId: Long,
  loadingStatus: InspectorLoadingStatus,
  latentUrl: String,
  discoveredSourceId: Option[Long],
  createdAt: Date,
  updatedAt: Date
)

object InspectorSourceParts {

  import InspectorSourceRecord.column

  implicit object insertable extends Insertable[InspectorSourceParts] {
    override def tableName: String = InspectorSourceRecord.table

    override def toContentValues(target: InspectorSourceParts): ContentValues =
      TypedFields.toContentValues(
        column.action_id -> target.actionId,
        column.source_loading_status -> target.loadingStatus,
        column.latent_source_url -> target.latentUrl,
        column.discovered_source_id -> target.discoveredSourceId,
        column.created_at -> target.createdAt,
        column.updated_at -> target.updatedAt
      )
  }

}
