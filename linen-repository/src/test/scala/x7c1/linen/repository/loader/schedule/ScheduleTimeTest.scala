package x7c1.linen.repository.loader.schedule

import java.util.{TimeZone, Calendar}

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.loader.schedule.ScheduleTime.{Minute, Hour}


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class ScheduleTimeTest extends JUnitSuiteLike {

  @Test
  def testCalendarAfter1() = {
    val time = ScheduleTime(Hour(13), Minute(30))
    def create() = {
      val current = Calendar getInstance TimeZone.getDefault
      current.set(Calendar.YEAR, 2016)
      current.set(Calendar.MONTH, 0)
      current.set(Calendar.DAY_OF_MONTH, 1)
      current.set(Calendar.HOUR_OF_DAY, 13)
      current
    }
    val beforeTarget = time calendarAfter {
      val current = create()
      current.set(Calendar.MINUTE, 15)
      current
    }
    assertEquals("2016-01-01T13:30:00+0900", Date(beforeTarget.getTime).format)

    val afterTarget = time calendarAfter {
      val current = create()
      current.set(Calendar.MINUTE, 45)
      current
    }
    assertEquals("2016-01-02T13:30:00+0900", Date(afterTarget.getTime).format)
  }

  @Test
  def testCalendarAfter2() = {
    val time = ScheduleTime(Hour(0), Minute(30))
    def create() = {
      val current = Calendar getInstance TimeZone.getDefault
      current.set(Calendar.YEAR, 2016)
      current.set(Calendar.MONTH, 0)
      current.set(Calendar.DAY_OF_MONTH, 1)
      current.set(Calendar.HOUR_OF_DAY, 0)
      current
    }
    val beforeTarget = time calendarAfter {
      val current = create()
      current.set(Calendar.MINUTE, 15)
      current
    }
    assertEquals("2016-01-01T00:30:00+0900", Date(beforeTarget.getTime).format)

    val afterTarget = time calendarAfter {
      val current = create()
      current.set(Calendar.MINUTE, 45)
      current
    }
    assertEquals("2016-01-02T00:30:00+0900", Date(afterTarget.getTime).format)
  }
}
