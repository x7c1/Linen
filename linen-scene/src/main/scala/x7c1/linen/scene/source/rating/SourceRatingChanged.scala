package x7c1.linen.scene.source.rating

import android.database.SQLException
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{HasChannelStatusKey, SourceRatingParts, source_ratings, source_statuses}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.source.setting.SettingSource
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.either.EitherTask
import x7c1.wheat.modern.callback.either.EitherTask.|
import x7c1.wheat.modern.database.Updatable
import x7c1.wheat.modern.database.selector.presets.RecyclerViewReloader
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

import scala.language.higherKinds


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
        column.rating -> target.rating
      )
    override def where(target: SourceRatingChanged) = toArgs (
      column.source_id -> target.sourceStatusKey.sourceId,
      column.account_id -> target.sourceStatusKey.accountId
    )
  }
}

class OnSourceRatingChanged[A: HasChannelStatusKey](
  helper: DatabaseHelper,
  reloader: RecyclerViewReloader[HasChannelStatusKey, SettingSource],
  key: A ) extends (SourceRatingChanged => Unit) {

  override def apply(event: SourceRatingChanged): Unit = {
    val task = for {
      _ <- new SourceRatingUpdater(helper).updateRating(event)
      _ <- reloader taskToRedraw key
    } yield {}

    task run {
      case Right(_) => // nop
      case Left(e) => Log error format(e){"[failed]"}
    }
  }
}

class SourceRatingUpdater(helper: DatabaseHelper){
  def updateRating(event: SourceRatingChanged): SQLException | Unit = {
    val found = helper.selectorOf[source_ratings] findBy event.sourceStatusKey
    val either = found.toEither.right flatMap {
      case Some(_) =>
        helper.writable update event
      case None =>
        helper.writable insert SourceRatingParts(
          accountId = event.sourceStatusKey.accountId,
          sourceId = event.sourceStatusKey.sourceId,
          rating = event.rating,
          createdAt = Date.current()
        )
    }
    EitherTask fromEither either.right.map(_ => {})
  }
}
