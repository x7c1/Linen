package x7c1.linen.repository.loader.schedule.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.LoaderScheduleWithKind
import x7c1.linen.database.struct.{LoaderScheduleLike, LoaderScheduleTimeRecord}
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, PresetLoaderSchedule, ScheduleTime}
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits._
import x7c1.wheat.modern.either.Imports._
import x7c1.wheat.modern.either.OptionEither

class ScheduleSelector(
  protected val db: SQLiteDatabase
) extends ScheduleTraverser with ScheduleFinder

trait ScheduleFinder {
  protected def db: SQLiteDatabase

  def findBy[A: LoaderScheduleLike](schedule: A): OptionEither[SQLException, LoaderSchedule] = {
    for {
      schedule <- db.selectorOf[LoaderScheduleWithKind].findBy(schedule)
      times <- db.selectorOf[LoaderScheduleTimeRecord].collectFrom(schedule).toOptionEither
    } yield PresetLoaderSchedule(
      record = schedule,
      times = times map ScheduleTime.fromRecord
    )
  }
}
