package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.database.struct.source_statuses.Key
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}
import x7c1.wheat.modern.database.{HasTable, Insertable, Updatable}


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

  object Key {
    implicit object id extends HasSourceStatusKey[Key] with IdEndo[Key]
  }
  case class Key(accountId:Long, sourceId: Long)

  implicit object hasTable extends HasTable[source_statuses]{
    override def tableName = table
  }
  implicit object providable extends DefaultProvidable[HasSourceStatusKey, source_statuses]

  implicit object reifiable extends RecordReifiable[source_statuses]{
    override def reify(cursor: Cursor) = TypedCursor[source_statuses](cursor)
  }
  implicit object findable extends CanFindRecord.Where[HasSourceStatusKey, source_statuses]{
    override def where[X](key: Key) = toArgs(
      column.source_id -> key.sourceId,
      column.account_id -> key.accountId
    )
  }
}

trait HasSourceStatusKey[A] extends Identifiable[A, Key]

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

    override def where(target: SourceStatusAsStarted) = toArgs(
      column.source_id -> target.sourceId,
      column.account_id -> target.accountId
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
