package x7c1.linen.repository.loader.schedule

import android.database.sqlite.{SQLiteDatabase, SQLiteException}
import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.wheat.modern.sequence.Sequence

class ScheduleRowSelector(val db: SQLiteDatabase){
  def collectBy[A: AccountIdentifiable](account: A): Either[SQLiteException, Sequence[LoaderScheduleRow]] = {
    Right(dummySchedules)
  }
  lazy val dummySchedules = Sequence from Seq(
    PresetLoaderSchedule(
      scheduleId = 111,
      name = "Load channels at..",
      enabled = true,
      startRanges = Sequence from Seq(
        TimeRange(3, 4),
        TimeRange(9, 10),
        TimeRange(15, 16)
      )
    )
  ) ++ createChannelSchedules

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
