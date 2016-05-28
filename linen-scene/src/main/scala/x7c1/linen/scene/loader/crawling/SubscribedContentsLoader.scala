package x7c1.linen.scene.loader.crawling

import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasLoaderScheduleId
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, PresetLoaderSchedule}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class SubscribedContentsLoader private (
  context: Context with ServiceControl,
  helper: DatabaseHelper ){

  def loadFromSchedule[A: HasLoaderScheduleId](schedule: A): Unit = {
    helper.selectorOf[LoaderSchedule] findBy schedule matches {
      case Right(Some(x: PresetLoaderSchedule)) =>
        QueueingService(context).loadSubscribedChannels(x.accountId)
        Log info s"[called] accountId:${x.accountId}"
      case Right(Some(x)) =>
        val id = implicitly[HasLoaderScheduleId[A]] toId schedule
        Log error s"not implemented yet (id:$id, name:${x.name}): $x"
      case Right(None) =>
        val id = implicitly[HasLoaderScheduleId[A]] toId schedule
        Log error s"schedule not found (id:$id)"
      case Left(e) =>
        Log error format(e){"[failed]"}
    }
  }
}

object SubscribedContentsLoader {
  def apply(
    context: Context with ServiceControl,
    helper: DatabaseHelper): SubscribedContentsLoader = {

    new SubscribedContentsLoader(context, helper)
  }
}
