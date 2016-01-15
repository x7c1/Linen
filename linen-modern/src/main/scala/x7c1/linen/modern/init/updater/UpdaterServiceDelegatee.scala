package x7c1.linen.modern.init.updater

import android.app.{NotificationManager, PendingIntent, Service}
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.NotificationCompat.Builder
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.modern.init.updater.UpdaterServiceDelegatee.ActionTypeSample
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky

object UpdaterServiceDelegatee {
  val ActionTypeSample = "hoge"
}

class UpdaterServiceDelegatee(service: Service with ServiceControl){

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }

  var notificationId: Int = 1

  def onStartCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info s"[init] start:$startId"

    val notificationIntent = new Intent(service, service getClassOf ServiceLabel.Updater)
    val pendingIntent = PendingIntent.getService(service, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
    val builder = new Builder(service).
      setContentIntent(pendingIntent).
      setContentTitle(s"sample title $notificationId").
      setContentText(s"sample text $notificationId").
      setProgress(100, notificationId, false).
      setSmallIcon(android.R.drawable.ic_dialog_info)

    val style0 = new NotificationCompat.InboxStyle(builder)
      .setSummaryText(s"summary $notificationId")
      .setBigContentTitle(s"title $notificationId")

    val style = (1 to notificationId).foldLeft(style0){
      case (s, i) => s.addLine(s"line $i")
    }
    val notification = style.build()

    val manager = service.getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

    Log info s"[n-id] $notificationId"
    service.startForeground(notificationId, notification)
    manager.notify(notificationId, notification)

    sendMessage(notificationId)

    notificationId += 1

    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
  }
  def sendMessage(n: Int) = {
    Log info s"[init] $n"
    val intent = new Intent(ActionTypeSample)
    intent.putExtra("sample-message", "hello!!!")
    LocalBroadcastManager.getInstance(service).sendBroadcast(intent)
  }

}
