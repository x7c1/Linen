package x7c1.linen.scene.updater

import android.app.{Notification, NotificationManager, PendingIntent, Service}
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.support.v4.app.NotificationCompat.Builder
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.logger.Log

class UpdaterServiceNotifier(
  service: Service with ServiceControl,
  max: Int,
  startTime: Date,
  notificationId: Int ){

  private def manager =
    service.getSystemService(NOTIFICATION_SERVICE).
      asInstanceOf[NotificationManager]

  def notifyDone(): Unit = {
    Log info s"[init] notificationId:$notificationId"
    /*
    val current = max
    val builder = createBuilder(current).setAutoCancel(true)
    val notification = createNotification(builder, current)
    */

    /*
      need to rebuild notification for kitkat device
        rf. http://stackoverflow.com/questions/27503863/swipe-dismissing-service-notification
     */
//    service.stopForeground(false/*removeNotification*/)
    /*
    service stopForeground {
      val removeNotification = true
      removeNotification
    }
    manager.notify(notificationId, notification)
    */

    /*
    // disable temporally for debug of scheduler
    manager.cancel(notificationId)
    */
  }

  def notifyProgress(current: Int): Unit = {
    val builder = createBuilder(current)
    val notification = createNotification(builder, current)

//    service.startForeground(notificationId, notification)
    manager.notify(notificationId, notification)
  }
  private def createNotification(builder: Builder, current: Int): Notification = {
    /*
    val style = new NotificationCompat.InboxStyle(builder)
      .setSummaryText(s"inserted $current/$max")
      .setBigContentTitle("Progress")

    style.build()
    */
    builder.build()
  }

  private def createPendingIntent = {
    val notificationIntent = new Intent(service, service getClassOf Updater)
    PendingIntent.getService(service, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
  }
  private def createBuilder(current: Int): Builder = {
    new Builder(service).
      setWhen {
        /* plus notificationId to identify same startTime */
        startTime.timestamp.toLong * 1000 + notificationId
      }.
      setContentIntent(createPendingIntent).
      setContentTitle(s"Progress $current/$max").
      setContentText(s"inserting").
      setProgress(max, current, false/*indeterminate*/).
      setSmallIcon(android.R.drawable.ic_dialog_info)
  }

  /*
  var notificationId: Int = 1

  def inboxSample() = {
    val notificationIntent = new Intent(service, service getClassOf Updater)
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
  }

  def sendMessage(n: Int) = {
    Log info s"[init] $n"
    val intent = new Intent(ActionTypeSample)
    intent.putExtra("sample-message", s"hello!!! $n")
    LocalBroadcastManager.getInstance(service).sendBroadcast(intent)
  }
  */

}
