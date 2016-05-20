package x7c1.linen.repository.loader.schedule

import java.util.Calendar

import x7c1.linen.repository.loader.schedule.ScheduleTime.{Hour, Minute}
import x7c1.wheat.modern.features.HasShortLength

case class ScheduleTime(
  hour: Hour,
  minute: Minute ){

  def format: String = {
    f"${hour.value}%02d:${minute.value}%02d"
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
    val before = create(original = base)
    val calendar =
      if (base.getTimeInMillis < before.getTimeInMillis){
        before
      } else {
        val after = create(original = before)
        after.add(Calendar.DAY_OF_MONTH, 1)
        after
      }

    calendar
  }
}
object ScheduleTime {

  case class Hour(value: Int)

  case class Minute(value: Int)
}

case class TimeRange(
  from: ScheduleTime,
  to: ScheduleTime ){

  def format: String = {
    s"${from.format} - ${to.format}"
  }
}

object TimeRange {
  def apply(from: Int, to: Int): TimeRange =
    TimeRange(
      from = ScheduleTime(Hour(from), Minute(0)),
      to = ScheduleTime(Hour(to), Minute(0))
    )

  implicit object short extends HasShortLength[TimeRange]
}
