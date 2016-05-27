package x7c1.linen.repository.loader.schedule

import java.util.Calendar

import x7c1.linen.database.struct.LoaderScheduleTimeRecord
import x7c1.linen.repository.loader.schedule.ScheduleTime.{Hour, Minute}
import x7c1.wheat.calendar.CalendarDate

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

  def fromRecord(record: LoaderScheduleTimeRecord): ScheduleTime = {
    ScheduleTime(
      Hour(record.start_hour),
      Minute(record.start_minute)
    )
  }
}

case class TimeRange(from: ScheduleTime){

  private val to = ScheduleTime(Hour(from.hour.value + 1))

  def format: String = {
    s"${from.format} - ${to.format}"
  }
}
