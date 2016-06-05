package x7c1.linen.repository.loader.schedule

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.LoaderScheduleWithKind
import x7c1.linen.repository.loader.schedule.selector.{PresetScheduleSelector, ScheduleRowSelector, ScheduleSelector}
import x7c1.wheat.calendar.CalendarDate
import x7c1.wheat.modern.database.selector.{CanProvideSelector, SelectorProvidable}
import x7c1.wheat.modern.features.HasShortLength

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
  private val times: Seq[ScheduleTime]) extends LoaderSchedule {

  import concurrent.duration._

  val startRanges: Seq[TimeRange] = times map TimeRange.apply

  private def calendarsOn(base: CalendarDate): Seq[CalendarDate] = {
    startRanges.map(_.from toCalendarDate base)
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
    times: Seq[ScheduleTime]): PresetLoaderSchedule = {

    PresetLoaderSchedule(
      scheduleId = record.schedule_id,
      accountId = record.account_id,
      name = "Load channels at..",
      enabled = record.enabled == 1,
      times = times
    )
  }
  implicit object providable
    extends SelectorProvidable[PresetLoaderSchedule, PresetScheduleSelector]
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
