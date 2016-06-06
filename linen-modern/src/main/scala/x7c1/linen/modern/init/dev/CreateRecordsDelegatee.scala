package x7c1.linen.modern.init.dev

import android.app.Activity
import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.support.v4.content.LocalBroadcastManager
import android.widget.Toast
import x7c1.linen.database.control.{DatabaseHelper, LinenDatabase}
import x7c1.linen.database.struct.source_statuses
import x7c1.linen.glue.res.layout.DevCreateRecordsLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.scene.updater.UpdaterMethods
import x7c1.linen.scene.updater.UpdaterServiceTypes.ActionTypeSample
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.AllowTruncate
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.formatter.ThrowableFormatter.format


class CreateRecordsDelegatee (
  activity: Activity with ServiceControl,
  layout: DevCreateRecordsLayout){

  private lazy val receiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent): Unit = {
      Log info s"${intent.getExtras}"
    }
  }
  private lazy val helper = new DatabaseHelper(activity)

  def getBroadcastManager = LocalBroadcastManager.getInstance(activity)

  def setup(): Unit = {
    getBroadcastManager.registerReceiver(
      receiver, new IntentFilter(ActionTypeSample))

    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    layout.createDummies onClick { _ =>
      ServiceCaller.using[UpdaterMethods].
        startService(activity, activity getClassOf Updater){
          _.createDummies(25)
        }
    }
    val selector = ChannelSelector(activity, helper){ e =>
      layout.selectedChannels.text = e.channelTitles mkString "\n"
    }
    layout.selectChannels onClick { _ =>
      selector.showDialog()
    }
    layout.createDummySources onClick { _ =>
      ServiceCaller.using[UpdaterMethods].
        startService(activity, activity getClassOf Updater){
          _ createDummySources selector.selectedChannelIds
        }
    }
    layout.createPresetJp onClick { _ =>
      ServiceCaller.using[UpdaterMethods].
        startService(activity, activity getClassOf Updater){
          _.createPresetJp()
        }
    }
    layout.createPreset onClick { _ =>
      ServiceCaller.using[UpdaterMethods].
        startService(activity, activity getClassOf Updater){
          _.createPresetJp()
        }
    }
    layout.markAllAsUnread onClick { _ =>
      implicit object truncate extends AllowTruncate[source_statuses]
      helper.writable.truncate[source_statuses] match {
        case Left(e) =>
          Log error format(e){"[failed]"}
          show(s"[failed] ${e.getMessage}")
        case Right(rows) =>
          Log info s"[done] deleted($rows)"
          show(s"[done] ${source_statuses.table} rows($rows) deleted")
      }
    }
    layout.deleteDatabase onClick { _ =>
      activity deleteDatabase LinenDatabase.name
      show("database deleted")
    }
  }
  def close(): Unit = {
    Log info "[done]"
    getBroadcastManager unregisterReceiver receiver
    helper.close()
  }
  private def show(message: String): Unit ={
    val toast = Toast.makeText(activity, message, Toast.LENGTH_LONG)
    toast.show()
  }
}
