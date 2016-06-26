package x7c1.linen.scene.loader.crawling

import android.app.Service
import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.loader.crawling.CrawlerFate
import x7c1.linen.repository.loader.schedule.LoaderSchedule
import x7c1.linen.repository.loader.schedule.setup.PresetScheduleSetup
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

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
    helper: DatabaseHelper ): SchedulerService = {

    new SchedulerServiceImpl(service, helper)
  }
}

private class SchedulerServiceImpl(
  service: Service with ServiceControl,
  helper: DatabaseHelper ) extends SchedulerService {

  override def setupSchedule(accountId: Long): Unit = CrawlerFate run {
    PresetScheduleSetup(helper) setupFor accountId
    setupAllSchedules()
  } atLeft {
    Log error _.detail
  }
  override def setupAllSchedules(): Unit = CrawlerFate run {
    helper.selectorOf[LoaderSchedule].traverseAll() match {
      case Right(schedules) =>
        // unsafe if so many schedules are added
        schedules.toSeq.map(_.scheduleId) foreach scheduleNextLoader
      case Left(e) =>
        Log error format(e){"[failed]"}
    }
  } atLeft {
    Log error _.detail
  }
  override def loadFromSchedule(scheduleId: Long): Unit = CrawlerFate run {

    /* setup schedule again for next call */
    scheduleNextLoader(scheduleId)

    SubscribedContentsLoader(service, helper) loadFromSchedule scheduleId
  } atLeft {
    Log error _.detail
  }
  override def scheduleNextLoader(scheduleId: Long): Unit = CrawlerFate run {
    LoaderScheduler(service, helper) setupNextLoader scheduleId
  } atLeft {
    Log error _.detail
  }
}
