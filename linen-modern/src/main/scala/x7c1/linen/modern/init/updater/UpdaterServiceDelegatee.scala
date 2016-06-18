package x7c1.linen.modern.init.updater

import android.app.Service
import android.content.Intent
import android.os.IBinder
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.loader.crawling.{RemoteSourceLoader, TraceableQueue}
import x7c1.linen.scene.loader.crawling.{QueueingService, SchedulerService}
import x7c1.linen.scene.updater.{ChannelNormalizerService, UpdaterMethods}
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky


class UpdaterServiceDelegatee(service: Service with ServiceControl){
  private lazy val helper = new DatabaseHelper(service)

  private lazy val queue = new TraceableQueue(helper, RemoteSourceLoader)

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }
  def onStartCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info s"[init] start:$startId, $intent"

    val expanders = Seq(
      IntentExpander from new UpdaterMethods(service, helper, startId),
      IntentExpander from QueueingService.reify(service, helper, queue),
      IntentExpander from SchedulerService.reify(service, helper),
      IntentExpander from ChannelNormalizerService.reify(service, helper)
    )
    expanders findRunnerOf intent match {
      case Left(e) => Log error e.message
      case Right(run) => run()
    }
    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
    helper.close()
  }
}
