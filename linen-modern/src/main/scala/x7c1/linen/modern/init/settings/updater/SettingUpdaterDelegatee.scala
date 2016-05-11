package x7c1.linen.modern.init.settings.updater

import android.app.Activity
import android.view.View
import android.view.View.OnClickListener
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingUpdaterLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.scene.updater.UpdaterMethods
import x7c1.wheat.macros.intent.{IntentExpander, ServiceCaller}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class SettingUpdaterDelegatee (
  activity: Activity with ActivityControl with ServiceControl,
  layout: SettingUpdaterLayout ){

  private lazy val helper = new DatabaseHelper(activity)

  def onCreate(): Unit = {
    Log info s"[init]"

    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    layout.toolbar.setTitle("Channel Updater")

    IntentExpander executeBy activity.getIntent
  }
  def onDestroy(): Unit = {
    Log info s"[init]"
    helper.close()
  }
  def setupFor(accountId: Long): Unit ={
    layout.updateChannels setOnClickListener new OnClickToLoadChannels(
      account = accountId,
      activity = activity,
      helper = helper
    )
  }
}

private class OnClickToLoadChannels[A: AccountIdentifiable](
  account: A,
  activity: Activity with ActivityControl with ServiceControl,
  helper: DatabaseHelper ) extends OnClickListener {

  private val accountId = implicitly[AccountIdentifiable[A]] toId account

  override def onClick(v: View): Unit = {
    Log info s"[init]"

    val caller = ServiceCaller.using[UpdaterMethods]

    helper.selectorOf[SubscribedChannel] traverseOn account match {
      case Left(e) => Log error format(e){"[failed]"}
      case Right(sequence) =>
        (0 to sequence.length - 1).view flatMap sequence.findAt foreach { channel =>
          Log info s"$channel"

          caller.startService(activity, activity getClassOf Updater){
            _.loadChannelSources(channel.channelId, accountId)
          }
        }
        sequence.closeCursor()
    }
  }
}
