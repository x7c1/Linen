package x7c1.linen.modern.init.updater

import java.util.concurrent.Executors

import android.app.Service
import android.content.Intent
import android.os.IBinder
import x7c1.linen.database.LinenOpenHelper
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.dev.DummyFactory
import x7c1.linen.repository.preset.PresetFactory
import x7c1.linen.repository.source.setting.SettingSourceAccessorFactory
import x7c1.wheat.modern.formatter.ThrowableFormatter
import ThrowableFormatter.format
import x7c1.wheat.macros.intent.{ExtraNotFound, IntentExpander}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky
import x7c1.wheat.modern.patch.TaskAsync.async

import scala.concurrent.Future

object LinenService {
  object Implicits {
    import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
    private lazy val pool = Executors.newCachedThreadPool()
    implicit def executor: ExecutionContextExecutor = ExecutionContext fromExecutor pool
  }
}

object UpdaterServiceDelegatee {
  val ActionTypeSample = "hoge"
}

class UpdaterServiceDelegatee(service: Service with ServiceControl){
  private lazy val helper = new LinenOpenHelper(service)
  private lazy val queue = new SourceUpdaterQueue(service, helper)

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

class UpdaterMethods(
  service: Service with ServiceControl,
  helper: LinenOpenHelper,
  queue: SourceUpdaterQueue,
  startId: Int){

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
  def createPresetJp(): Unit = async {
    Log info "[init]"
    new PresetFactory(helper).setupJapanesePresets()
  }
  def createDummySources(channelIds: Seq[Long]) = async {
    Log info s"$channelIds"
  }
  def loadSource(sourceId: Long): Unit = {
    Log info s"[init] source-id: $sourceId"

    import LinenService.Implicits._
    val inspector = SourceInspector(helper)
    val future = Future { inspector inspectSource sourceId } map {
      case Right(source) => queue enqueue source
      case Left(error) => Log error error.message
    }
    future onFailure {
      case e => Log error format(e)(s"[error] source(id:$sourceId)")
    }
  }
  def loadChannelSources(channelId: Long, accountId: Long): Unit = async {
    Log info s"[init] channel:$channelId"

    val factory = new SettingSourceAccessorFactory(helper.getReadableDatabase, accountId)
    val accessor = factory.create(channelId)
    (0 to accessor.length - 1) flatMap accessor.findAt foreach { source =>
      loadSource(source.sourceId)
    }
  }
}


