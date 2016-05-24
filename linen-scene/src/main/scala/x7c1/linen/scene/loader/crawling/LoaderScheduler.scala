package x7c1.linen.scene.loader.crawling

import android.content.{Context, Intent}
import android.net.Uri
import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.loader.schedule.PresetLoaderSchedule
import x7c1.wheat.calendar.CalendarDate
import x7c1.wheat.macros.intent.IntentBuilder.from
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.chrono.alarm.WindowAlarm

class LoaderScheduler[A: AccountIdentifiable] private (
  context: Context with ServiceControl, account: A){

  import concurrent.duration._

  def createOrUpdate(schedule: PresetLoaderSchedule) = {
    Log info s"[init] $schedule"

    schedule nextStartAfter CalendarDate.now() match {
      case Some(start) =>
        WindowAlarm(
          context = context,
          window = 1.hour,
//          window = 10.seconds,
          start = start
        ) perform createIntent(schedule)
      case None =>
        Log warn s"time not found: (schedule:${schedule.scheduleId})"
    }
  }
  private def createIntent(schedule: PresetLoaderSchedule): Intent = {
    val accountId = implicitly[AccountIdentifiable[A]] toId account
    val intent = SchedulerService(context) buildIntent from {
      _.loadFromSchedule(schedule.scheduleId, accountId)
    }
    intent setData Uri.parse(
      s"linen://loader.schedule/setup/${schedule.accountId}/${schedule.scheduleId}"
    )
    intent
  }

}

object LoaderScheduler {
  def apply[A: AccountIdentifiable]
    (context: Context with ServiceControl, account: A): LoaderScheduler[A] = {

    new LoaderScheduler(
      context = context,
      account: A
    )
  }
}
