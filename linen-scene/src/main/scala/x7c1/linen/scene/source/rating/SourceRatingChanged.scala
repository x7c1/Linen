package x7c1.linen.scene.source.rating

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{source_ratings, source_statuses}
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.Updatable
import x7c1.wheat.modern.formatter.ThrowableFormatter.format


case class SourceRatingChanged(
  sourceStatusKey: source_statuses.Key,
  rating: Int
)

object SourceRatingChanged {
  import source_ratings.column
  implicit object updatable extends Updatable[SourceRatingChanged]{
    override def tableName: String = source_ratings.table
    override def toContentValues(target: SourceRatingChanged) =
      TypedFields toContentValues (
        column.rating -> target.rating,
        column.source_id -> target.sourceStatusKey.sourceId,
        column.account_id -> target.sourceStatusKey.accountId
      )
    override def where(target: SourceRatingChanged) = toArgs (
      column.source_id -> target.sourceStatusKey.sourceId,
      column.account_id -> target.sourceStatusKey.accountId
    )
  }
}

class SourceRatingUpdater(helper: DatabaseHelper){
  def onSourceRatingChanged(event: SourceRatingChanged): Unit = {
    Log info event.toString
    helper.writable update event match {
      case Left(error) => format(error){"[failed]"}
      case Right(rows) => Log info s"updated ($rows rows)"
    }
  }
}
