package x7c1.linen.scene.updater

import android.app.Service
import android.content.Intent
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.crawler.{Implicits, SourceInspector, SourceUpdaterQueue}
import x7c1.linen.repository.dummy.DummyFactory
import x7c1.linen.repository.preset.PresetFactory
import x7c1.linen.repository.source.setting.SettingSourceAccessorFactory
import x7c1.wheat.macros.intent.{ExtraNotFound, IntentExpander}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.patch.TaskAsync.async

import scala.concurrent.Future

class UpdaterMethods(
  service: Service with ServiceControl,
  helper: DatabaseHelper,
  queue: SourceUpdaterQueue,
  startId: Int){

  import Implicits._

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
