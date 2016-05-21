package x7c1.wheat.modern.chrono.alarm

import java.util.Calendar

import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.{AlarmManager, PendingIntent}
import android.content.{Context, Intent}

import scala.concurrent.duration.Duration

class WindowAlarm private (context: Context, startMilliSeconds: Long, window: Duration){

  private lazy val manager = context.
    getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

  def perform(intent: Intent): Unit = {
    require(
      Option(intent.getData).isDefined,
      message = "Intent#setData not called?"
    )
    val operation = {
      /*
        always use same requestCode since each operation is
        identified by intent#getData, not by this requestCode.
      */
      val requestCode = 0
      PendingIntent.getService(context, requestCode, intent, FLAG_CANCEL_CURRENT)
    }
    manager.setWindow(
      AlarmManager.RTC_WAKEUP,
      startMilliSeconds,
      window.toMillis,
      operation
    )
  }
}

object WindowAlarm {
  def apply(context: Context, start: Calendar, window: Duration): WindowAlarm = {
    new WindowAlarm(context, start.getTimeInMillis, window)
  }
}
