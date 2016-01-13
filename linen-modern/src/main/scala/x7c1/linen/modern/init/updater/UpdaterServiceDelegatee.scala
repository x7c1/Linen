package x7c1.linen.modern.init.updater

import android.app.Service
import android.content.Intent
import android.os.IBinder
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky

class UpdaterServiceDelegatee(service: Service){

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }
  def onStartCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info "[init]"

//    val notification = new Notification()
    
    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
  }
}
