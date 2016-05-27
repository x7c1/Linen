package x7c1.linen.modern.init.settings.schedule

import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.LoaderScheduleParts.ToChangeState
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.scene.loader.crawling.LoaderScheduler
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class OnScheduleStateChanged (context: Context with ServiceControl, helper: DatabaseHelper){
  def onStateChanged(event: ScheduleStateChanged): Unit = {
    Log info s"[init]"

    val scheduler = LoaderScheduler(
      context = context,
      helper = helper
    )
    val either = helper.writable update ToChangeState(
      scheduleId = event.scheduleId,
      enabled = event.enabled
    )
    either.left foreach { e =>
      Log error format(e.getCause){"[failed]"}
    }
    if (event.enabled){
      scheduler setupNextLoader event.scheduleId
    } else {
      scheduler cancelSchedule event.scheduleId
    }
  }
}
