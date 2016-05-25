package x7c1.linen.scene.loader.crawling

import android.app.Service
import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.loader.crawling.Implicits._
import x7c1.linen.repository.loader.schedule.LoaderSchedule
import x7c1.linen.repository.loader.schedule.setup.PresetScheduleSetup
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

import scala.concurrent.Future

trait SchedulerService {
  def setupSchedule(accountId: Long): Unit
  def setupAllSchedules(): Unit
  def scheduleNextLoader(scheduleId: Long): Unit
  def loadFromSchedule(scheduleId: Long): Unit
}

object SchedulerService {

  def apply(context: Context with ServiceControl) =
    ServiceCaller.reify[SchedulerService](
      context,
      context getClassOf ServiceLabel.Updater
    )

  def reify(
    service: Service with ServiceControl,
    helper: DatabaseHelper,
    startId: Int ): SchedulerService = {

    new SchedulerServiceImpl(service, helper, startId)
  }
}

private class SchedulerServiceImpl(
  service: Service with ServiceControl,
  helper: DatabaseHelper,
  startId: Int ) extends SchedulerService {

  override def setupSchedule(accountId: Long): Unit = Future {
    PresetScheduleSetup(helper) setupFor accountId
    setupAllSchedules()
  } onFailure {
    case e => Log error format(e){"[abort] (unexpected)"}
  }
  override def setupAllSchedules(): Unit = Future {
    helper.selectorOf[LoaderSchedule].traverseAll() match {
      case Right(schedules) =>
        // unsafe if so many schedules are added
        schedules.toSeq.map(_.scheduleId) foreach scheduleNextLoader
      case Left(e) =>
        Log error format(e){"[failed]"}
    }

    /*
    Log error s"[init] account:$accountId"

    helper.selectorOf[LoaderSchedule] findPresetSchedule accountId matches {
      case Right(Some(schedule)) => LoaderScheduler(service, accountId) createOrUpdate schedule
      case Right(None) => Log error s"preset schedule not found"
      case Left(e) => Log error format(e){"[failed]"}
    }
    */
  } onFailure {
    case e => Log error format(e){"[abort] (unexpected)"}
  }

  override def loadFromSchedule(scheduleId: Long): Unit = Future {

    /* setup schedule again for next call */
    scheduleNextLoader(scheduleId)

    SubscribedChannelsLoader(service, helper) loadFromSchedule scheduleId

    /*
    Log info s"schedule:$scheduleId, account:$accountId"

    /* setup schedule again for next call */
//    setupAllSchedules(accountId)
    scheduleLoader(scheduleId)

    SubscribedChannelsLoader(service, helper).execute(accountId)
    */
  } onFailure {
    case e => Log error format(e){"[abort] (unexpected)"}
  }

  override def scheduleNextLoader(scheduleId: Long): Unit = Future {
    LoaderScheduler(service, helper) setupNextLoader scheduleId
  } onFailure {
    case e => Log error format(e){"[abort] (unexpected)"}
  }
}
