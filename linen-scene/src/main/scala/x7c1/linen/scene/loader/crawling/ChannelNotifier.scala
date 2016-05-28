package x7c1.linen.scene.loader.crawling

import android.app.{PendingIntent, Service}
import android.content.Intent
import android.support.v4.app.NotificationCompat.Builder
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.date.Date
import x7c1.wheat.modern.decorator.Imports._

class ChannelNotifier (
  service: Service with ServiceControl,
  startTime: Date,
  channelName: String,
  notificationId: Int ){

  def notifyProgress(current: Int, max: Int): Unit = {
    val builder = createBuilder(current, max)
    val notification = builder.build()
    service.notificationManager.notify(notificationId, notification)
  }
  def notifyDone(): Unit = {

  }
  private def createPendingIntent = {
    val notificationIntent = new Intent(service, service getClassOf ServiceLabel.Updater)
    PendingIntent.getService(service, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
  }
  private def createBuilder(current: Int, max: Int): Builder = {
    new Builder(service).
      setWhen {
        /* plus notificationId to identify same startTime */
        startTime.timestamp.toLong * 1000 + notificationId
      }.
      setContentIntent(createPendingIntent).
      setContentTitle(s"Channel : $channelName").
      setContentText(s"loading.. $current/$max").
      setProgress(max, current, false/*indeterminate*/).
      setSmallIcon(android.R.drawable.ic_dialog_info)
  }
}
