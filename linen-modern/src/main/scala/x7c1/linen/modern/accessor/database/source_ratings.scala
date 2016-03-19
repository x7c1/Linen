package x7c1.linen.modern.accessor.database

import android.content.ContentValues
import x7c1.linen.modern.accessor.Insertable
import x7c1.linen.modern.struct.Date



case class SourceRatingParts(
  sourceId: Long,
  accountId: Long,
  rating: Int,
  createdAt: Date
)

object SourceRatingParts {
  implicit object insertable extends Insertable[SourceRatingParts] {
    override def tableName: String = "source_ratings"
    override def toContentValues(target: SourceRatingParts): ContentValues = {
      val values = new ContentValues()
      values.put("source_id", target.sourceId: java.lang.Long)
      values.put("account_id", target.accountId: java.lang.Long)
      values.put("rating", target.rating: java.lang.Integer)
      values.put("created_at", target.createdAt.timestamp: java.lang.Integer)
      values
    }
  }
}
