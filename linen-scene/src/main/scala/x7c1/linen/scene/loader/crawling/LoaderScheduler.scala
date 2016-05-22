package x7c1.linen.scene.loader.crawling

import java.util.{Calendar, TimeZone}

import android.app.Service
import android.content.Context
import android.net.Uri
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.loader.crawling.Implicits
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, PresetLoaderSchedule}
import x7c1.wheat.macros.intent._
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.chrono.alarm.WindowAlarm
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

import scala.concurrent.Future

class LoaderScheduler[A: AccountIdentifiable] private (
  context: Context, control: ServiceControl, account: A){

  def createOrUpdate(schedule: PresetLoaderSchedule) = {
    Log info s"[init] $schedule"

    schedule.startRanges.toSeq.foreach { range =>
      import concurrent.duration._

      val alarm = WindowAlarm(
        context = context,
        window = 1.hour,
        start = {
          val current = Calendar getInstance TimeZone.getDefault
          range.from calendarAfter current
        }
      )
      val accountId = implicitly[AccountIdentifiable[A]] toId account
      val intent = IntentFactory.using[SchedulerService].
        create(context, control getClassOf ServiceLabel.Updater){
          _.loadFromSchedule(schedule.scheduleId, accountId)
        }

      /*
        use intent.setData() to distinguish
        pendingIntent among different schedules
      */

      intent setData Uri.parse(
        s"linen://loader.schedule/setup/${schedule.scheduleId}/${range.startTimeId}"
      )
      alarm perform intent
    }
  }

}
object LoaderScheduler {
  def apply[A: AccountIdentifiable]
    (service: Service with ServiceControl, account: A): LoaderScheduler[A] = {

    new LoaderScheduler(
      context = service,
      control = service,
      account: A
    )
  }
}
