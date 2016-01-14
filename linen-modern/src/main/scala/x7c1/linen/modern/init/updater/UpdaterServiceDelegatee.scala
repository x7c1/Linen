package x7c1.linen.modern.init.updater

import android.app.Notification.Builder
import android.app.{Notification, NotificationManager, PendingIntent, Service}
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.{Bundle, Handler, IBinder, Message, Messenger, RemoteException}
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.modern.init.dev.CreateRecordsDelegatee.{MessageTypeForProgress, MessageTypeForRegister, MessageTypeForSet, MessageTypeForUnregister}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky

import scala.collection.mutable.ListBuffer

class UpdaterServiceDelegatee(service: Service with ServiceControl){

  private val clients = ListBuffer[Messenger]()
  private val messenger = new Messenger(new IncomingHandler2(clients))

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    Some(messenger.getBinder)
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

    val style0 = new Notification.InboxStyle(builder)
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

    clients foreach { client =>
      val message = Message.obtain(null, MessageTypeForProgress)
      message setData {
        val bundle = new Bundle()
        bundle.putString("hoge-", s"hoge-id:$notificationId")
        bundle
      }
      client.send(message)
    }
    notificationId += 1

    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
  }
}

class IncomingHandler2(clients: ListBuffer[Messenger]) extends Handler {

  override def handleMessage(msg: Message): Unit = {
    Log info s"[init] $msg"

    msg.what match {
      case MessageTypeForRegister =>
        clients append msg.replyTo
      case MessageTypeForSet =>
        clients foreach { messenger =>
          try {
            messenger.send(Message.obtain(null, MessageTypeForSet, msg.arg1, 0))
          } catch {
            case e: RemoteException =>
              Log error e.getMessage
          }
        }
      case MessageTypeForUnregister =>
        clients indexOf msg.replyTo match {
          case i if i < 0 => Log error s"unknown message ${msg.replyTo}"
          case i => clients remove i
        }
      case _ =>
        super.handleMessage(msg)
    }
  }
}
