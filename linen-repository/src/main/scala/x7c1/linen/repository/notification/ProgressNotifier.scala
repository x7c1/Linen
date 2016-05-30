package x7c1.linen.repository.notification

import android.app.PendingIntent
import android.content.{Context, Intent}
import android.support.v4.app.NotificationCompat.Builder
import x7c1.wheat.calendar.CalendarDate
import x7c1.wheat.modern.decorator.Imports._

class ProgressNotifier private (
  context: Context,
  startTime: CalendarDate,
  notificationId: Int ){

  def show(content: ProgressContent): Unit = {
    val operation = {
      val intent = content.intent
      val requestCode = 0
      PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
    }
    val builder = new Builder(context).
      setWhen(startTime.toMilliseconds).
      setContentIntent(operation).
      setContentTitle(content.title).
      setContentText(content.text).
      setProgress(content.max, content.progress, false/*indeterminate*/).
      setSmallIcon(content.icon)

    val notification = builder.build()
    context.notificationManager.notify(notificationId, notification)
  }
}
object ProgressNotifier {
  def apply(
    context: Context,
    startTime: CalendarDate,
    notificationId: Int): ProgressNotifier = {

    new ProgressNotifier(context, startTime, notificationId)
  }
}

case class ProgressContent (
  title: String,
  text: String,
  max: Int,
  progress: Int,
  intent: Intent,
  icon: Int = android.R.drawable.ic_dialog_info
)
