package x7c1.linen.modern.init.dev

import android.app.Activity
import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.support.v4.content.LocalBroadcastManager
import android.widget.Toast
import x7c1.linen.glue.res.layout.DevCreateRecordsLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.accessor.LinenDatabase
import x7c1.linen.modern.init.updater.UpdaterServiceDelegatee
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.patch.TaskAsync.async

class CreateRecordsDelegatee (
  activity: Activity with ServiceControl,
  layout: DevCreateRecordsLayout){

  lazy val receiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent): Unit = {
      val x = intent.getStringExtra("sample-message")
      Log info x
      Log info s"$intent"
    }
  }
  def getBroadcastManager = LocalBroadcastManager.getInstance(activity)

  def setup(): Unit = {
    getBroadcastManager.registerReceiver(
      receiver, new IntentFilter(UpdaterServiceDelegatee.ActionTypeSample))

    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    layout.createDummies onClick { _ =>
      async {
        Log info "start"
        val intent = new Intent(activity, activity getClassOf Updater)
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
    getBroadcastManager unregisterReceiver receiver
  }
}
