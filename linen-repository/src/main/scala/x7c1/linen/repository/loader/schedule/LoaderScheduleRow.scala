package x7c1.linen.repository.loader.schedule

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.LoaderScheduleWithKind
import x7c1.linen.repository.loader.schedule.selector.{PresetScheduleSelector, ScheduleRowSelector, ScheduleSelector}
import x7c1.wheat.calendar.CalendarDate
import x7c1.wheat.modern.database.selector.{CanProvideSelector, SelectorProvidable}
import x7c1.wheat.modern.features.HasShortLength
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
  def accountId: Long
  def name: String
  def enabled: Boolean
  def nextStartAfter(current: CalendarDate): Option[CalendarDate]
}

object LoaderSchedule {
  implicit object providable extends CanProvideSelector[LoaderSchedule]{
    override type Selector = ScheduleSelector
    override def createFrom(db: SQLiteDatabase) = new ScheduleSelector(db)
  }
  implicit object short extends HasShortLength[LoaderSchedule]
}

case class PresetLoaderSchedule(
  scheduleId: Long,
  accountId: Long,
  name: String,
  enabled: Boolean,
  startRanges: Sequence[TimeRange]) extends LoaderSchedule {

  import concurrent.duration._

  private def calendarsOn(base: CalendarDate): Seq[CalendarDate] = {
    startRanges.toSeq.map(_.from toCalendarDate base)
  }
  override def nextStartAfter(current: CalendarDate): Option[CalendarDate] = {
    val tomorrow = current + 1.day
    val baseTimes = calendarsOn(current)
    val nextTimes = calendarsOn(tomorrow)
    (baseTimes ++ nextTimes) find {_ > current}

    /*
    val debug = CalendarDate.now()
    Some(debug + 10.seconds)
    // */
  }
}
object PresetLoaderSchedule {
  def apply(
    record: LoaderScheduleWithKind,
    ranges: Sequence[TimeRange]): PresetLoaderSchedule = {

    PresetLoaderSchedule(
      scheduleId = record.schedule_id,
      accountId = record.account_id,
      name = "Load channels at..",
      enabled = record.enabled == 1,
      startRanges = ranges
    )
  }
  implicit object providable
    extends SelectorProvidable[PresetLoaderSchedule, PresetScheduleSelector](
      new PresetScheduleSelector(_)
    )

}

case class ChannelLoaderSchedule(
  scheduleId: Long,
  accountId: Long,
  name: String,
  enabled: Boolean ) extends LoaderSchedule {

  override def nextStartAfter(current: CalendarDate) = None
}

case class SourceLoaderSchedule(
  scheduleId: Long,
  accountId: Long,
  name: String,
  enabled: Boolean ) extends LoaderSchedule {

  override def nextStartAfter(current: CalendarDate) = None
}
