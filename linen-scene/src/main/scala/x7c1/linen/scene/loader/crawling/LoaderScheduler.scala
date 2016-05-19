package x7c1.linen.scene.loader.crawling

import java.util.Calendar

import android.app.{Service, AlarmManager, PendingIntent}
import android.content.Context
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.loader.schedule.PresetLoaderSchedule
import x7c1.linen.scene.updater.UpdaterMethods
import x7c1.wheat.macros.intent.IntentFactory
import x7c1.wheat.macros.logger.Log

class LoaderScheduler private (context: Context, control: ServiceControl){
  def createOrUpdate(schedule: PresetLoaderSchedule) = {
    schedule.startRanges.toSeq.foreach { range =>
      Log info "h:" + range.from.hour.value
      Log info "m:" + range.from.minute.value

      val manager = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
      val pendingIntent = {
        val intent = IntentFactory.using[UpdaterMethods].
          create(context, control getClassOf ServiceLabel.Updater){
            _.loadFromSchedule(schedule.scheduleId)
          }

        /*
          todo:
            use intent.setData() to distinguish
            pendingIntent among different schedules
         */

        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
      }
      val msec = {
        val calendar = Calendar.getInstance()
        calendar setTimeInMillis System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 10)
        calendar.getTimeInMillis
      }
      manager.set(AlarmManager.RTC_WAKEUP, msec, pendingIntent)
    }
  }
}

object LoaderScheduler {
  def apply(service: Service with ServiceControl): LoaderScheduler = {
    new LoaderScheduler(
      context = service,
      control = service
    )
  }
}
