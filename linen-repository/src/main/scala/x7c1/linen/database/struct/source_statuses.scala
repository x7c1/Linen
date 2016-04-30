package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{SingleWhere, Insertable, Updatable}


trait source_statuses extends TypedFields {
  def source_id: Long
  def account_id: Long
  def start_entry_id: Long
  def start_entry_created_at: Int
  def created_at: Int --> Date
}
object source_statuses {
  def column: source_statuses = TypedFields.expose[source_statuses]
  def table: String = "source_statuses"

  case class Key(accountId:Long, sourceId: Long)

  implicit object selectable extends SingleWhere[source_statuses, Key](table){
    override def where(id: Key) = Seq(
      "source_id" -> id.sourceId.toString,
      "account_id" -> id.accountId.toString
    )
    override def fromCursor(cursor: Cursor) = {
      TypedCursor[source_statuses](cursor).freezeAt(0)
    }
  }
}

case class SourceStatusParts(
  sourceId: Long,
  accountId: Long,
  createdAt: Date
)
object SourceStatusParts {
  import source_statuses.column
  implicit object insertable extends Insertable[SourceStatusParts] {

    override def tableName = source_statuses.table

    override def toContentValues(target: SourceStatusParts) =
      TypedFields toContentValues (
        column.source_id -> target.sourceId,
        column.account_id -> target.accountId,
        column.created_at -> target.createdAt
      )
  }
}

case class SourceStatusAsStarted(
  startEntryId: Long,
  startEntryCreatedAt: Int,
  sourceId: Long,
  accountId: Long
)
object SourceStatusAsStarted {
  import source_statuses.column
  implicit object updatable extends Updatable[SourceStatusAsStarted] {

    override def tableName = source_statuses.table

    override def toContentValues(target: SourceStatusAsStarted) =
      TypedFields toContentValues (
        column.start_entry_id -> target.startEntryId,
        column.start_entry_created_at -> target.startEntryCreatedAt
      )

    override def where(target: SourceStatusAsStarted) = Seq(
      "source_id" -> target.sourceId.toString,
      "account_id" -> target.accountId.toString
    )
  }
  implicit object insertable extends Insertable[SourceStatusAsStarted]{

    override def tableName = source_statuses.table

    override def toContentValues(target: SourceStatusAsStarted) =
      TypedFields toContentValues (
        column.start_entry_id -> target.startEntryId,
        column.start_entry_created_at -> target.startEntryCreatedAt,
        column.source_id -> target.sourceId,
        column.account_id -> target.accountId,
        column.created_at -> Date.current()
      )
  }
}
