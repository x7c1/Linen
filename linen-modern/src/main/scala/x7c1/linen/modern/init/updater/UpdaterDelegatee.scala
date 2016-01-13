package x7c1.linen.modern.init.updater

import android.app.Service
import android.content.Intent
import android.os.IBinder
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky

class UpdaterDelegatee(service: Service){

  def create(): Unit = {
    Log info "[init]"
  }
  def destroy(): Unit = {
    Log info "[init]"
  }
  def setupBinder(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }
  def startCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info "[init]"
    NotSticky
  }
}
