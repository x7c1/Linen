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

trait InspectorActionRecord extends TypedFields {
  def action_id: Long

  def action_loading_status: Int --> InspectorLoadingStatus

  def account_id: Long

  def origin_title: String

  def origin_url: String

  def created_at: Int --> Date

  def updated_at: Int --> Date
}

object InspectorActionRecord {
  def table = "inspector_actions"

  def column = TypedFields.expose[InspectorActionRecord]

  implicit object providable
    extends SelectorProvidable[InspectorActionRecord, Selector]

  implicit object reifiable extends RecordReifiable[InspectorActionRecord] {
    override def reify(cursor: Cursor) = TypedCursor[InspectorActionRecord](cursor)
  }

  implicit object traverseAll extends CanTraverseRecordByQuery[InspectorActionRecord](
    sql"SELECT * FROM inspector_actions"
  )

  class Selector(protected val db: SQLiteDatabase)
    extends TraverseAll[InspectorActionRecord]

}

case class InspectorActionParts(
  loadingStatus: InspectorLoadingStatus,
  accountId: Long,
  originTitle: String,
  originUrl: String,
  createdAt: Date,
  updatedAt: Date
)

object InspectorActionParts {
  import InspectorActionRecord.column

  implicit object insertable extends Insertable[InspectorActionParts] {
    override def tableName: String = InspectorActionRecord.table

    override def toContentValues(target: InspectorActionParts): ContentValues =
      TypedFields.toContentValues(
        column.action_loading_status -> target.loadingStatus,
        column.account_id -> target.accountId,
        column.origin_title -> target.originTitle,
        column.origin_url -> target.originUrl,
        column.created_at -> target.createdAt,
        column.updated_at -> target.updatedAt
      )
  }

}
