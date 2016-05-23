package x7c1.linen.repository.loader.schedule

import java.util.Calendar

import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.selector.CanProvideSelector
import x7c1.wheat.modern.sequence.Sequence

sealed trait LoaderScheduleRow

object LoaderScheduleRow {
  implicit object providable extends CanProvideSelector[LoaderScheduleRow]{
    override type Selector = ScheduleRowSelector
    override def createFrom(db: SQLiteDatabase) = new ScheduleRowSelector(db)
  }
}

trait LoaderSchedule extends LoaderScheduleRow {
  def scheduleId: Long
  def name: String
  def enabled: Boolean
}

object LoaderSchedule {
  implicit object providable extends CanProvideSelector[LoaderSchedule]{
    override type Selector = ScheduleSelector
    override def createFrom(db: SQLiteDatabase) = new ScheduleSelector(db)
  }
}

case class PresetLoaderSchedule(
  scheduleId: Long,
  accountId: Long,
  name: String,
  enabled: Boolean,
  startRanges: Sequence[TimeRange]) extends LoaderSchedule {

  private def calendarsOf(base: Calendar): Seq[Calendar] = {
    startRanges.toSeq.map(_.from toCalendar base)
  }
  def findNextStart(current: Calendar): Option[Calendar] = {
    val tomorrow = {
      val x = Calendar getInstance current.getTimeZone
      x setTimeInMillis current.getTimeInMillis
      x.add(Calendar.DAY_OF_MONTH, 1)
      x
    }
    val baseTimes = calendarsOf(current)
    val nextTimes = calendarsOf(tomorrow)
    (baseTimes ++ nextTimes) find { _.getTimeInMillis > current.getTimeInMillis }

    /*
    val debug = Calendar getInstance TimeZone.getDefault
    debug.setTimeInMillis(current.getTimeInMillis)
    debug.add(Calendar.SECOND, 10)
    Some(debug)
    // */
  }
}

case class ChannelLoaderSchedule(
  scheduleId: Long,
  name: String,
  enabled: Boolean ) extends LoaderSchedule

case class SourceLoaderSchedule(
  scheduleId: Long,
  name: String,
  enabled: Boolean ) extends LoaderSchedule
