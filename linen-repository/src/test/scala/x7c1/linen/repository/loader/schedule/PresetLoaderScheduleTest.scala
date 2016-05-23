package x7c1.linen.repository.loader.schedule

import java.util.{Calendar, TimeZone}

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.loader.schedule.ScheduleTime.{Hour, Minute}
import x7c1.wheat.modern.sequence.Sequence


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetLoaderScheduleTest extends JUnitSuiteLike {

  @Test
  def testFindNextStart() = {
    val schedule = createPreset(accountId = 123)

    // 2016-01-01 13:00
    def create() = {
      val current = Calendar getInstance TimeZone.getDefault
      current.set(Calendar.YEAR, 2016)
      current.set(Calendar.MONTH, 0)
      current.set(Calendar.DAY_OF_MONTH, 1)
      current.set(Calendar.HOUR_OF_DAY, 13)
      current
    }
    assertEquals("2016-01-01T21:00:00+0900", {
      val Some(calendar) = schedule findNextStart {
        // 13:15
        val x = create()
        x.set(Calendar.MINUTE, 15)
        x
      }
      Date(calendar.getTime).format
    })
    assertEquals("2016-01-01T05:00:00+0900", {
      val Some(calendar) = schedule findNextStart {
        // 04:00
        val x = create()
        x.set(Calendar.HOUR_OF_DAY, 4)
        x
      }
      Date(calendar.getTime).format
    })
    assertEquals("2016-01-02T05:00:00+0900", {
      val Some(calendar) = schedule findNextStart {
        // 23:00
        val x = create()
        x.set(Calendar.HOUR_OF_DAY, 23)
        x
      }
      Date(calendar.getTime).format
    })

    val emptySchedule = schedule.copy(
      startRanges = Sequence from Seq()
    )
    assertEquals(None, emptySchedule findNextStart (current = create()))
  }

  private def createPreset(accountId: Long) = PresetLoaderSchedule(
    scheduleId = 777,
    accountId = accountId,
    name = "Load channels at..",
    enabled = true,
    startRanges = Sequence from Seq(
      TimeRange(
        startTimeId = 222,
        ScheduleTime(Hour(5), Minute(0))
      ),
      TimeRange(
        startTimeId = 333,
        ScheduleTime(Hour(13), Minute(0))
      ),
      TimeRange(
        startTimeId = 444,
        ScheduleTime(Hour(21), Minute(0))
      )
    )
  )
}
