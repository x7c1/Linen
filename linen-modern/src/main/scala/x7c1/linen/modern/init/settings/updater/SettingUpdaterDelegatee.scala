package x7c1.linen.modern.init.settings.updater

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
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
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.intent.{IntentExpander, ServiceCaller}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.sequence.Sequence

class SettingUpdaterDelegatee (
  activity: Activity with ActivityControl with ServiceControl,
  layout: SettingUpdaterLayout,
  scheduleRowProviders: LoaderScheduleRowProviders ){

  private lazy val helper = new DatabaseHelper(activity)

  def onCreate(): Unit = {
    Log info s"[init]"

    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    layout.toolbar.setTitle("Loader Schedules")

    IntentExpander executeBy activity.getIntent
  }
  def onDestroy(): Unit = {
    Log info s"[init]"
    helper.close()
  }
  def setupFor(accountId: Long): Unit = {
    layout.updateChannels setOnClickListener new OnClickToLoadChannels(
      account = accountId,
      activity = activity,
      helper = helper
    )
    val dummySchedules = Sequence from Seq(
      LoaderSchedule(name = "Load all channels", enabled = true),
      LoaderSchedule(name = "Load source : WIRED.jp", enabled = true)
    )
    layout.schedules setLayoutManager new LinearLayoutManager(activity)
    layout.schedules setAdapter new ScheduleRowAdapter(
      delegatee = AdapterDelegatee.create(scheduleRowProviders, dummySchedules)
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
        sequence.toSeq foreach { channel =>
          Log info s"$channel"

          caller.startService(activity, activity getClassOf Updater){
            _.loadChannelSources(channel.channelId, accountId)
          }
        }
        sequence.closeCursor()
    }
  }
}
