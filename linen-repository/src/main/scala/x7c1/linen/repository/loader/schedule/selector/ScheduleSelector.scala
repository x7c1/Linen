package x7c1.linen.repository.loader.schedule.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.LoaderScheduleWithKind
import x7c1.linen.database.struct.{LoaderScheduleLike, LoaderScheduleTimeRecord}
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, PresetLoaderSchedule, TimeRange}
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits._
import x7c1.wheat.modern.either.Imports._
import x7c1.wheat.modern.either.OptionEither

class ScheduleSelector(
  protected val db: SQLiteDatabase
) extends ScheduleTraverser with ScheduleFinder

  /*
  private def dummySchedules(accountId: Long): Sequence[LoaderSchedule] = Sequence from Seq(
    preset(accountId)
  ) ++ createChannelSchedules(accountId)

  private def preset(accountId: Long) = PresetLoaderSchedule(
    scheduleId = 777,
    accountId = accountId,
    name = "Load channels at..",
    enabled = true,
    startRanges = Sequence from Seq(
      /*
      TimeRange(
        ScheduleTime(Hour(22), Minute(55))
      ),
      TimeRange(
        ScheduleTime(Hour(22), Minute(56))
      ),
      TimeRange(
        ScheduleTime(Hour(23), Minute(18))
      ),
      */
      TimeRange(
        ScheduleTime(Hour(5), Minute(0))
      ),
      TimeRange(
        ScheduleTime(Hour(13), Minute(0))
      ),
      TimeRange(
        ScheduleTime(Hour(21), Minute(0))
      )
    )
  )
  private def createChannelSchedules(accountId: Long) = {
    (0 to 20) map { n =>
      ChannelLoaderSchedule(
        scheduleId = 111 * n,
        accountId = accountId,
        name = s"Load channel : $n",
        enabled = true
      )
    }
  }
  */

trait ScheduleFinder {
  protected def db: SQLiteDatabase

  def findBy[A: LoaderScheduleLike](schedule: A): OptionEither[SQLException, LoaderSchedule] = {
    for {
      schedule <- db.selectorOf[LoaderScheduleWithKind].findBy(schedule)
      times <- db.selectorOf[LoaderScheduleTimeRecord].collectFrom(schedule).toOptionEither
    } yield PresetLoaderSchedule(
      record = schedule,
      ranges = TimeRange fromTimeRecords times
    )
  }

}
