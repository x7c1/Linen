package x7c1.wheat.calendar

import java.util.{Calendar, Date, TimeZone}

import scala.concurrent.duration.Duration

class CalendarDate private (underlying: Calendar) extends Ordered[CalendarDate]{

  def year   = underlying.get(Calendar.YEAR)
  def month  = underlying.get(Calendar.MONTH) + 1
  def day    = underlying.get(Calendar.DAY_OF_MONTH)
  def hour   = underlying.get(Calendar.HOUR_OF_DAY)
  def minute = underlying.get(Calendar.MINUTE)
  def second = underlying.get(Calendar.SECOND)
  def millisecond = underlying.get(Calendar.MILLISECOND)

  def + (duration: Duration): CalendarDate = {
    val target = Calendar getInstance underlying.getTimeZone
    target setTimeInMillis (toMilliseconds + duration.toMillis)
    new CalendarDate(target)
  }
  def copy(
    year: Int = year,
    month: Int = month,
    day: Int = day,
    hour: Int = hour,
    minute: Int = minute,
    second: Int = second,
    millisecond: Int = millisecond ): CalendarDate = {

    val x = Calendar.getInstance(underlying.getTimeZone)
    x.set(Calendar.YEAR, year)
    x.set(Calendar.MONTH, month - 1)
    x.set(Calendar.DAY_OF_MONTH, day)
    x.set(Calendar.HOUR_OF_DAY, hour)
    x.set(Calendar.MINUTE, minute)
    x.set(Calendar.SECOND, second)
    x.set(Calendar.MILLISECOND, millisecond)
    new CalendarDate(x)
  }
  def toMilliseconds: Long = underlying.getTimeInMillis

  def toDate: Date = underlying.getTime

  override def compare(that: CalendarDate): Int = {
    toMilliseconds compare that.toMilliseconds
  }
}

object CalendarDate {
  def now() = apply(TimeZone.getDefault)

  def apply(tz: TimeZone): CalendarDate = {
    new CalendarDate(Calendar getInstance tz)
  }
}
