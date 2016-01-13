package x7c1.linen.modern.init.updater

import android.app.Notification.Builder
import android.app.{NotificationManager, PendingIntent, Service}
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.IBinder
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky

class UpdaterServiceDelegatee(service: Service with ServiceControl){

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }
  def onStartCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info "[init]"

    val notificationIntent = new Intent(service, service getClassOf Updater)
    val pendingIntent = PendingIntent.getService(service, 0, notificationIntent, 0)
    val builder = new Builder(service).
      setContentIntent(pendingIntent).
      setContentTitle("sample linen title").
      setContentText("sample linen test").
      setSmallIcon(android.R.drawable.ic_dialog_info)

    val notification = builder.build()
    val manager = service.getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    manager.notify(123, notification)
    service.startForeground(123, notification)

    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
  }
}
