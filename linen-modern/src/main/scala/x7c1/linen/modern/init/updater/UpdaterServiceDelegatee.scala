package x7c1.linen.modern.init.updater

import android.app.Service
import android.content.Intent
import android.os.IBinder
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.init.dev.DummyFactory
import x7c1.wheat.macros.intent.{ExtraNotFound, IntentExpander}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky
import x7c1.wheat.modern.patch.TaskAsync.async

object UpdaterServiceDelegatee {
  val ActionTypeSample = "hoge"
}

class UpdaterServiceDelegatee(service: Service with ServiceControl){

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }

  def onStartCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info s"[init] start:$startId, $intent"

    new UpdaterMethods(service, startId) execute intent

    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
  }
}
class UpdaterMethods(service: Service with ServiceControl, startId: Int){

  def execute(intent: Intent) = IntentExpander findFrom intent match {
    case Left(e: ExtraNotFound) => Log error e.toString
    case Left(notFound) => Log info notFound.toString
    case Right(f) => f.apply()
  }

  def createDummies(max: Int): Unit = async {
    Log info "[init]"
    val notifier = new UpdaterServiceNotifier(service, max)
    DummyFactory.createDummies0(service)(max){ n =>
      notifier.notifyProgress(n)
    }
    notifier.notifyDone()
    service stopSelf startId
  }
}

/*
case class Hoge123(name: String)

object Hoge123 {
  implicit object convertible extends BundleConvertible[Hoge123] {
    override def toBundle(target: Hoge123): Bundle = {
      val x = new Bundle()
      x.putString("name", target.name)
      x
    }
  }
}
*/