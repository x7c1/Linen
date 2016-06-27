package x7c1.linen.database.struct

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.source_statuses.Key
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.{HasTable, Insertable}
import x7c1.wheat.modern.database.selector.{RecordReifiable, SelectorProvidable}
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, FindBy}

trait source_ratings extends TypedFields {
  def source_id: Long
  def account_id: Long
  def rating: Int
  def created_at: Int --> Date
}

object source_ratings {
  def table = "source_ratings"

  def column = TypedFields.expose[source_ratings]

  implicit object hasTable extends HasTable.Where[source_ratings](table)

  implicit object providable extends SelectorProvidable[source_ratings, Selector]

  implicit object reifiable extends RecordReifiable[source_ratings]{
    override def reify(cursor: Cursor) = TypedCursor[source_ratings](cursor)
  }
  implicit object find extends CanFindRecord.Where[HasSourceStatusKey, source_ratings]{
    override def where[X](key: Key) = TypedFields.toArgs(
      column.source_id -> key.sourceId,
      column.account_id -> key.accountId
    )
  }
  class Selector(
    protected val db: SQLiteDatabase) extends FindBy[HasSourceStatusKey, source_ratings]
}

case class SourceRatingParts(
  sourceId: Long,
  accountId: Long,
  rating: Int,
  createdAt: Date
)

object SourceRatingParts {
  import source_ratings.column
  implicit object insertable extends Insertable[SourceRatingParts] {
    override def tableName: String = source_ratings.table
    override def toContentValues(target: SourceRatingParts): ContentValues = {
      TypedFields toContentValues (
        column.source_id -> target.sourceId,
        column.account_id -> target.accountId,
        column.rating -> target.rating,
        column.created_at -> target.createdAt
      )
    }
  }
}
