package x7c1.linen.repository.loader.schedule

import java.util.TimeZone

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.loader.schedule.ScheduleTime.{Hour, Minute}
import x7c1.wheat.calendar.CalendarDate


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetLoaderScheduleTest extends JUnitSuiteLike {

  @Test
  def testFindNextStart() = {
    val schedule = createPreset(accountId = 123)

    // 2016-01-01 13:00:00
    val current = CalendarDate(TimeZone.getDefault).copy(
      year = 2016,
      month = 1,
      day = 1,
      hour = 13,
      minute = 0,
      second = 0
    )
    assertEquals("2016-01-01T21:00:00+0900", {
      val Some(calendar) = schedule nextStartAfter {
        // 13:15
        current copy (minute = 15)
      }
      Date(calendar.toDate).format
    })
    assertEquals("2016-01-01T05:00:00+0900", {
      val Some(calendar) = schedule nextStartAfter {
        // 04:00
        current copy (hour = 4)
      }
      Date(calendar.toDate).format
    })
    assertEquals("2016-01-02T05:00:00+0900", {
      val Some(calendar) = schedule nextStartAfter {
        // 23:00
        current copy (hour = 23)
      }
      Date(calendar.toDate).format
    })

    val emptySchedule = schedule.copy(
      times = Seq()
    )
    assertEquals(None, emptySchedule nextStartAfter current)
  }

  private def createPreset(accountId: Long) = PresetLoaderSchedule(
    scheduleId = 777,
    accountId = accountId,
    name = "Load channels at..",
    enabled = true,
    times = Seq(
      ScheduleTime(Hour(5), Minute(0)),
      ScheduleTime(Hour(13), Minute(0)),
      ScheduleTime(Hour(21), Minute(0))
    )
  )
}
