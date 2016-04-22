package x7c1.linen.modern.init.updater

import android.app.Service
import android.content.Intent
import android.os.IBinder
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.crawler.SourceUpdaterQueue
import x7c1.linen.scene.updater.UpdaterMethods
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky


class UpdaterServiceDelegatee(service: Service with ServiceControl){
  private lazy val helper = new DatabaseHelper(service)
  private lazy val queue = new SourceUpdaterQueue(helper)

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }

  def onStartCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info s"[init] start:$startId, $intent"

    new UpdaterMethods(service, helper, queue, startId) execute intent
    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
    helper.close()
  }
}




