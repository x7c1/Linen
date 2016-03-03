package x7c1.linen.modern.init.dev

import android.app.Activity
import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.support.v4.content.LocalBroadcastManager
import android.widget.Toast
import x7c1.linen.glue.res.layout.DevCreateRecordsLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.accessor.{LinenOpenHelper, LinenDatabase}
import x7c1.linen.modern.init.updater.{UpdaterMethods, UpdaterServiceDelegatee}
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class CreateRecordsDelegatee (
  activity: Activity with ServiceControl,
  layout: DevCreateRecordsLayout){

  private lazy val receiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent): Unit = {
      Log info s"${intent.getExtras}"
    }
  }
  private lazy val helper = new LinenOpenHelper(activity)

  def getBroadcastManager = LocalBroadcastManager.getInstance(activity)

  def setup(): Unit = {
    getBroadcastManager.registerReceiver(
      receiver, new IntentFilter(UpdaterServiceDelegatee.ActionTypeSample))

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
    layout.deleteDatabase onClick { _ =>
      activity deleteDatabase LinenDatabase.name
      Toast.makeText(activity, "database deleted", Toast.LENGTH_SHORT).show()
    }
  }
  def close(): Unit = {
    Log info "[done]"
    getBroadcastManager unregisterReceiver receiver
    helper.close()
  }
}
