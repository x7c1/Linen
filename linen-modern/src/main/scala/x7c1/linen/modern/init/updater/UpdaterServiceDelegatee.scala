package x7c1.linen.modern.init.updater

import android.app.{NotificationManager, PendingIntent, Service}
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.NotificationCompat.Builder
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.modern.init.dev.DummyFactory
import x7c1.linen.modern.init.updater.UpdaterServiceDelegatee.ActionTypeSample
import x7c1.wheat.macros.intent.MethodCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky
import x7c1.wheat.modern.patch.TaskAsync.async

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
    Log info s"[init] start:$startId, $intent"

    Option(intent.getAction) match {
      case Some(_) =>
        new UpdaterMethods(service, startId) executeBy intent
      case None =>
        service stopForeground true
    }
    NotSticky
  }
  def showNotification() = {
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
  }
  def onDestroy(): Unit = {
    Log info "[init]"
  }
  def sendMessage(n: Int) = {
    Log info s"[init] $n"
    val intent = new Intent(ActionTypeSample)
    intent.putExtra("sample-message", s"hello!!! $n")
    LocalBroadcastManager.getInstance(service).sendBroadcast(intent)
  }

}
class UpdaterMethods(service: Service with ServiceControl, startId: Int){

  def executeBy(intent: Intent): Unit = MethodCaller using intent

  def createDummies(): Unit = async {
    Log info "[init]"
    val max = 100
    DummyFactory.createDummies0(service)(max){ n =>
      notifyProgress(max, n)
    }
    service stopSelf startId
  }
  private def manager =
    service.getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

  private def notifyProgress(max: Int, current: Int): Unit ={
    val notificationIntent = new Intent(service, service getClassOf ServiceLabel.Updater)
    val pendingIntent =
      PendingIntent.getService(service, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

    val builder = new Builder(service).
      setContentIntent(pendingIntent).
      setContentTitle(s"Progress $current/$max").
      setContentText(s"inserting").
      setProgress(100, current, false).
      setSmallIcon(android.R.drawable.ic_dialog_info)

    val style0 = new NotificationCompat.InboxStyle(builder)
      .setSummaryText(s"inserted $current/$max")
      .setBigContentTitle("Progress")

    val notification = style0.build()

    service.startForeground(123, notification)
    manager.notify(123, notification)
  }

  def baz: Unit = {
    Log info "baz"
  }
  def foo(): Unit = {
    Log info s"Hello!"
  }
  def hello(arg1: String): Unit = {
    Log info s"Hello, $arg1!"
  }
  def sample_?(arg1_! : String, arg2: Int, arg3: Long): Unit = {
    Log info s"sample, ${arg1_!}, $arg2, $arg3"
  }
  def bar(x: Int)(y: Long): Unit = {
    Log info s"Hello!"
  }
}

/*
case class Hoge123(name: String)

object Hoge123 {
  implicit object convertible extends BundleConvertible[Hoge123] {
    override def toBundle(target: Hoge123): Bundle = {
      val x = new Bundle()
      x.putString("name", target.name)
      x
    }
  }
}
*/
