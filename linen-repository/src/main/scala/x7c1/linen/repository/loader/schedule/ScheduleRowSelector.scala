package x7c1.linen.repository.loader.schedule

import android.database.sqlite.{SQLiteDatabase, SQLiteException}
import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.linen.repository.loader.schedule.ScheduleTime.{Minute, Hour}
import x7c1.wheat.modern.database.selector.CanProvideSelector
import x7c1.wheat.modern.either.{OptionRight, OptionEither}
import x7c1.wheat.modern.sequence.Sequence

class ScheduleRowSelector(val db: SQLiteDatabase){

  def selectorOf[A](implicit x: CanProvideSelector[A]): x.Selector = x createFrom db

  def collectBy[A: AccountIdentifiable](account: A): Either[SQLiteException, Sequence[LoaderScheduleRow]] = {
    selectorOf[LoaderSchedule].collectBy(account)
  }
}

class ScheduleSelector(val db: SQLiteDatabase){
  def collectBy[A: AccountIdentifiable](account: A): Either[SQLiteException, Sequence[LoaderSchedule]] = {
    Right(dummySchedules)
  }
  def findPresetSchedule[A: AccountIdentifiable](account: A): OptionEither[SQLiteException, PresetLoaderSchedule] = {
    OptionRight(preset)
  }
  lazy val dummySchedules = Sequence from Seq(
    preset
  ) ++ createChannelSchedules

  private lazy val preset = PresetLoaderSchedule(
    scheduleId = 777,
    name = "Load channels at..",
    enabled = true,
    startRanges = Sequence from Seq(
      /*
      TimeRange(
        startTimeId = 101,
        ScheduleTime(Hour(22), Minute(55))
      ),
      TimeRange(
        startTimeId = 102,
        ScheduleTime(Hour(22), Minute(56))
      ),
      TimeRange(
        startTimeId = 103,
        ScheduleTime(Hour(23), Minute(18))
      ),
      */
      TimeRange(
        startTimeId = 222,
        ScheduleTime(Hour(5), Minute(0))
      ),
      TimeRange(
        startTimeId = 333,
        ScheduleTime(Hour(13), Minute(0))
      ),
      TimeRange(
        startTimeId = 444,
        ScheduleTime(Hour(21), Minute(0))
      )
    )
  )
  private def createChannelSchedules = {
    (0 to 20) map { n =>
      ChannelLoaderSchedule(
        scheduleId = 111 * n,
        name = s"Load channel : $n",
        enabled = true
      )
    }
  }

}
