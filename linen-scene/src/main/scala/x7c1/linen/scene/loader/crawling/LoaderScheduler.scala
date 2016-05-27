package x7c1.linen.scene.loader.crawling

import android.content.{Context, Intent}
import android.net.Uri
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.LoaderScheduleLike
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.loader.schedule.LoaderSchedule
import x7c1.wheat.calendar.CalendarDate
import x7c1.wheat.macros.intent.IntentBuilder.from
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.chrono.alarm.WindowAlarm
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class LoaderScheduler private (
  context: Context with ServiceControl,
  helper: DatabaseHelper ){

  import concurrent.duration._

  def setupNextLoader[A: LoaderScheduleLike](schedule: A): Unit = {
    find(schedule) foreach { existent =>
      createOrUpdate(existent)
    }
  }
  private def find[A: LoaderScheduleLike](schedule: A) = {
    helper.selectorOf[LoaderSchedule] findBy schedule matches {
      case Right(Some(existent)) =>
        Some(existent)
      case Right(None) =>
        val scheduleId = implicitly[LoaderScheduleLike[A]] toId schedule
        Log error s"schedule not found (id:$scheduleId)"
        None
      case Left(e) =>
        Log error format(e.getCause){"[failed]"}
        None
    }
  }
  private def createAlarmOf(schedule: LoaderSchedule) = {
    WindowAlarm(
      context = context,
      intent = createIntent(schedule)
    )
  }
  private def createOrUpdate(schedule: LoaderSchedule) = {
    Log info s"[init] $schedule"

    schedule nextStartAfter CalendarDate.now() match {
      case Some(start) =>
        createAlarmOf(schedule) triggerInTime (
          window = 1.hour,
          startMilliSeconds = start.toMilliseconds

          /* debug
          window = 10.seconds,
          startMilliSeconds = (CalendarDate.now() + 10.seconds).toMilliseconds
          // */
        )
      case None =>
        Log warn s"time not found: (schedule:${schedule.scheduleId})"
    }
  }
  private def createIntent(schedule: LoaderSchedule): Intent = {
    val intent = SchedulerService(context) buildIntent from {
      _.loadFromSchedule(schedule.scheduleId)
    }
    intent setData Uri.parse(
      s"linen://loader.schedule/setup/${schedule.accountId}/${schedule.scheduleId}"
    )
    intent
  }

}

object LoaderScheduler {
  def apply(context: Context with ServiceControl, helper: DatabaseHelper): LoaderScheduler = {
    new LoaderScheduler(
      context = context,
      helper = helper
    )
  }
}
