package x7c1.linen.repository.loader.schedule

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.LoaderScheduleWithKind
import x7c1.linen.database.struct.{AccountIdentifiable, LoaderScheduleLike, LoaderScheduleTimeRecord}
import x7c1.linen.repository.loader.schedule.ScheduleTime.{Hour, Minute}
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits._
import x7c1.wheat.modern.database.selector.presets.ClosableSequence
import x7c1.wheat.modern.either.{OptionEither, OptionRight}
import x7c1.wheat.modern.either.Imports._
import x7c1.wheat.modern.sequence.Sequence

class ScheduleRowSelector(db: SQLiteDatabase){
  def collectBy[A: AccountIdentifiable](account: A): Either[SQLException, Sequence[LoaderScheduleRow]] = {
    db.selectorOf[LoaderSchedule].collectBy(account)
  }
}

class ScheduleSelector(val db: SQLiteDatabase){

  def collectBy[A: AccountIdentifiable](account: A): Either[SQLException, Sequence[LoaderSchedule]] = {
    val accountId = implicitly[AccountIdentifiable[A]] toId account
    Right(dummySchedules(accountId))
  }
  def traverseAll(): Either[SQLException, ClosableSequence[LoaderSchedule]] = {
    LoaderSchedules toTraverseAll db
  }
  def findBy[A: LoaderScheduleLike](schedule: A): OptionEither[SQLException, LoaderSchedule] = {
    for {
      schedule <- db.selectorOf[LoaderScheduleWithKind].findBy(schedule)
      times <- db.selectorOf[LoaderScheduleTimeRecord].collectFrom(schedule).toOptionEither
    } yield PresetLoaderSchedule(
      record = schedule,
      ranges = TimeRange fromTimeRecords times
    )
  }
  def findPresetSchedule[A: AccountIdentifiable](account: A): OptionEither[SQLException, PresetLoaderSchedule] = {
    val accountId = implicitly[AccountIdentifiable[A]] toId account
    OptionRight(preset(accountId))
  }
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

}
