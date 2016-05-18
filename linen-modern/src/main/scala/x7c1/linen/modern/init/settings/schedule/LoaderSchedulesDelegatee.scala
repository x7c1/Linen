package x7c1.linen.modern.init.settings.schedule

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingScheduleLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.loader.schedule.{ChannelLoaderSchedule, PresetLoaderSchedule, TimeRange}
import x7c1.linen.scene.updater.UpdaterMethods
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.intent.{IntentExpander, ServiceCaller}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.menu.popup.{PopupMenuBox, PopupMenuItem}
import x7c1.wheat.modern.sequence.Sequence

class LoaderSchedulesDelegatee (
  activity: Activity with ActivityControl with ServiceControl,
  layout: SettingScheduleLayout,
  scheduleRowProviders: LoaderScheduleRowProviders,
  timeRowProviders: ScheduleTimeRowProviders){

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
    layout.schedules setLayoutManager new LinearLayoutManager(activity)
    layout.schedules setAdapter new ScheduleRowAdapter(
      delegatee = AdapterDelegatee.create(scheduleRowProviders, dummySchedules),
      providers = timeRowProviders,
      onMenuSelected = showMenu(accountId)
    )
  }
  lazy val dummySchedules = Sequence from Seq(
    PresetLoaderSchedule(
      scheduleId = 111,
      name = "Load subscribed channels at..",
      enabled = true,
      startRanges = Sequence from Seq(
        TimeRange(3, 4),
        TimeRange(9, 10),
        TimeRange(15, 16)
      )
    )
  ) ++ createChannelSchedules

  private def createChannelSchedules = {
    (0 to 20) map { n =>
      ChannelLoaderSchedule(
        scheduleId = 111 * n,
        name = s"Load channel : $n",
        enabled = true
      )
    }
  }
  private def showMenu[A: AccountIdentifiable]
    (account: A)(event: ScheduleSelected) = {

    val loadNow = PopupMenuItem("Load now"){ _ =>
      new SubscribedChannelsLoader(activity, helper) execute account
    }
    val items = Seq(
      loadNow
    )
    PopupMenuBox(activity, event.targetView, items).show()
  }
}

private class SubscribedChannelsLoader(
  activity: Activity with ActivityControl with ServiceControl,
  helper: DatabaseHelper ){

  def execute[A: AccountIdentifiable](account: A): Unit = {
    Log info s"[init]"

    val accountId = implicitly[AccountIdentifiable[A]] toId account
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