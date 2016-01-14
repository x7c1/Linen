package x7c1.linen.modern.init.dev

import android.app.Activity
import android.content.Context.BIND_AUTO_CREATE
import android.content.{ComponentName, Intent, ServiceConnection}
import android.os.{Handler, Message, RemoteException, Messenger, IBinder}
import android.widget.Toast
import x7c1.linen.glue.res.layout.DevCreateRecordsLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.accessor.LinenDatabase
import x7c1.linen.modern.init.dev.CreateRecordsDelegatee.{MessageTypeForUnregister, MessageTypeForSet, MessageTypeForRegister}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.patch.TaskAsync.async

object CreateRecordsDelegatee {
  val MessageTypeForRegister = 1
  val MessageTypeForSet = 2
  val MessageTypeForProgress = 3
  val MessageTypeForUnregister = 3
}

class CreateRecordsDelegatee (
  activity: Activity with ServiceControl,
  layout: DevCreateRecordsLayout){

  private var serviceMessenger: Option[Messenger] = None

  private val handlerMessenger = new Messenger(new IncomingHandler)

  lazy val connection = new ServiceConnection {
    override def onServiceConnected(name: ComponentName, service: IBinder): Unit = {
      serviceMessenger = Some apply new Messenger(service)
      try {
        val message = Message.obtain(null, MessageTypeForRegister)
        message.replyTo = handlerMessenger
        serviceMessenger foreach (_ send message)

        val message2 = Message.obtain(null, MessageTypeForSet, this.hashCode(), 0)
        serviceMessenger foreach (_ send message2)
      } catch {
        case e: RemoteException => Log error e.getMessage
      }
      Log info "[done]"
    }
    override def onServiceDisconnected(name: ComponentName): Unit = {
      Log info "[done]"
    }
  }
  def setup(): Unit = {
    val intent = new Intent(activity, activity getClassOf Updater)
    activity.bindService(intent, connection, BIND_AUTO_CREATE)

    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    layout.createDummies onClick { _ =>
      async {
        Log info "start"
        activity.startService(intent)
      }
    }
    layout.deleteDatabase onClick { _ =>
      activity deleteDatabase LinenDatabase.name
      Toast.makeText(activity, "database deleted", Toast.LENGTH_SHORT).show()
    }
  }

  def close(): Unit = {
    Log info "[done]"

    serviceMessenger foreach { messenger =>
      try {
        val message = Message.obtain(null, MessageTypeForUnregister)
        message.replyTo = handlerMessenger
        messenger send message
      } catch {
        case e: RemoteException => Log error e.getMessage
      }
    }
    activity unbindService connection
  }
}

class IncomingHandler extends Handler {
  override def handleMessage(msg: Message): Unit = {
    msg.what match {
      case MessageTypeForSet =>
        Log info s"received sample ${msg.arg1} $msg"
      case _ =>
        Log info s"received other $msg, ${msg.getData}"
    }
  }
}
