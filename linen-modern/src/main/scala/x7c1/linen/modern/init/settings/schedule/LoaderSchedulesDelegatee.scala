package x7c1.linen.modern.init.settings.schedule

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingScheduleLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.loader.schedule.LoaderScheduleRow
import x7c1.linen.scene.loader.crawling.SubscribedContentsLoader
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.intent.IntentExpander
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
    helper.selectorOf[LoaderScheduleRow] traverseOn accountId match {
      case Right(schedules) =>
        val adapter = createAdapter(accountId, schedules)
        layout.schedules setLayoutManager new LinearLayoutManager(activity)
        layout.schedules setAdapter adapter
      case Left(e) =>
        Log error format(e){"[failed]"}
    }
  }
  private def createAdapter[A: AccountIdentifiable]
    (account: A, schedules: Sequence[LoaderScheduleRow]) = {

    new ScheduleRowAdapter(
      delegatee = AdapterDelegatee.create(
        providers = scheduleRowProviders,
        sequence = schedules
      ),
      providers = timeRowProviders,
      onMenuSelected = showMenu(account),
      onStateChanged = new OnScheduleStateChanged(activity, helper).onStateChanged
    )
  }
  private def showMenu[A: AccountIdentifiable]
    (account: A)(event: ScheduleSelected) = {

    val loadNow = PopupMenuItem("Load now"){ _ =>
      SubscribedContentsLoader(activity, helper) loadFromSchedule event
    }
    val items = Seq(
      loadNow
    )
    PopupMenuBox(activity, event.targetView, items).show()
  }
}
