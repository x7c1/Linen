package x7c1.linen.repository.loader.schedule

import java.util.Calendar

import x7c1.linen.database.struct.LoaderScheduleTimeRecord
import x7c1.linen.repository.loader.schedule.ScheduleTime.{Hour, Minute}
import x7c1.wheat.calendar.CalendarDate
import x7c1.wheat.modern.features.HasShortLength
import x7c1.wheat.modern.sequence.Sequence

case class ScheduleTime(
  hour: Hour,
  minute: Minute = Minute(0) ){

  def format: String = {
    f"${hour.value}%02d:${minute.value}%02d"
  }
  def toCalendarDate(base: CalendarDate): CalendarDate = {
    base.copy(
      hour = hour.value,
      minute = minute.value
    )
  }
  def calendarAfter(base: Calendar): Calendar = {
    def create(original: Calendar) = {
      val x = Calendar getInstance original.getTimeZone
      x.setTime(original.getTime)
      x.set(Calendar.HOUR_OF_DAY, hour.value)
      x.set(Calendar.MINUTE, minute.value)
      x.set(Calendar.SECOND, 0)
      x
    }
    val baseDate = create(original = base)
    val calendar =
      if (base.getTimeInMillis < baseDate.getTimeInMillis){
        baseDate
      } else {
        val nextDate = create(original = baseDate)
        nextDate.add(Calendar.DAY_OF_MONTH, 1)
        nextDate
      }

    calendar
  }
}
object ScheduleTime {

  case class Hour(value: Int)

  case class Minute(value: Int)
}

case class TimeRange(from: ScheduleTime){

  private val to = ScheduleTime(Hour(from.hour.value + 1))

  def format: String = {
    s"${from.format} - ${to.format}"
  }
}

object TimeRange {
  implicit object short extends HasShortLength[TimeRange]

  def fromTimeRecords(times: Seq[LoaderScheduleTimeRecord]) =
    Sequence from times.map { time =>
      TimeRange(
        ScheduleTime(
          Hour(time.start_hour),
          Minute(time.start_minute)
        )
      )
    }
}
