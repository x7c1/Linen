package x7c1.wheat.modern.chrono.alarm

import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.{AlarmManager, PendingIntent}
import android.content.{Context, Intent}

import scala.concurrent.duration.Duration

class WindowAlarm private (context: Context, intent: Intent){
  require(
    Option(intent.getData).isDefined,
    message = "Intent#setData not called?"
  )
  private lazy val manager = context.
    getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

  def triggerInTime(startMilliSeconds: Long, window: Duration): Unit = {
    manager.setWindow(
      AlarmManager.RTC_WAKEUP,
      startMilliSeconds,
      window.toMillis,
      operationBy(intent)
    )
  }
  def cancel(): Unit = {
    manager cancel operationBy(intent)
  }
  def operationBy(intent: Intent) = {
    /*
      always use same requestCode since each operation is
      identified by intent#getData, not by this requestCode.
    */
    val requestCode = 0
    PendingIntent.getService(context, requestCode, intent, FLAG_CANCEL_CURRENT)
  }
}

object WindowAlarm {
  def apply(context: Context, intent: Intent): WindowAlarm = {
    new WindowAlarm(context, intent)
  }
}
