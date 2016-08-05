package x7c1.linen.database.struct

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.{CanTraverseRecord, TraverseOn}
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

  implicit object reifiable extends RecordReifiable[InspectorActionRecord]{
    override def reify(cursor: Cursor) = TypedCursor[InspectorActionRecord](cursor)
  }
  implicit object traverseOn extends CanTraverseRecord[HasAccountId, InspectorActionRecord]{
    override def queryAbout[X: HasAccountId](target: X) = {
      ???
    }
  }
  class Selector(protected val db: SQLiteDatabase)
    extends TraverseOn[HasAccountId, InspectorActionRecord]
}



case class InspectorActionParts()