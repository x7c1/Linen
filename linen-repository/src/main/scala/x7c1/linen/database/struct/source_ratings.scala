package x7c1.linen.database.struct

import android.content.ContentValues
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.modern.database.Insertable

trait source_ratings extends TypedFields {
  def source_id: Long
  def account_id: Long
  def rating: Int
  def created_at: Int --> Date
}

object source_ratings {
  def table = "source_ratings"
  def column = TypedFields.expose[source_ratings]
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
