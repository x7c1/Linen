package x7c1.linen.modern.init.updater

import android.app.Service
import android.content.Intent
import android.os.IBinder
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.dummy.TraceableQueue
import x7c1.linen.repository.loader.crawling.RemoteSourceLoader
import x7c1.linen.scene.loader.crawling.LoaderSchedulerMethods
import x7c1.linen.scene.updater.UpdaterMethods
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
      IntentExpander from new UpdaterMethods(service, helper, queue, startId),
      IntentExpander from new LoaderSchedulerMethods(service, helper, startId)
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
