package x7c1.linen.repository.loader.schedule

import android.database.sqlite.{SQLiteDatabase, SQLiteException}
import x7c1.linen.database.struct.AccountIdentifiable
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
    scheduleId = 111,
    name = "Load channels at..",
    enabled = true,
    startRanges = Sequence from Seq(
      TimeRange(5, 6),
      TimeRange(13, 14),
      TimeRange(21, 22)
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
