package x7c1.linen.scene.updater

import java.util.Calendar

import android.app.{PendingIntent, AlarmManager, Service}
import android.content.{Context, Intent}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.{ServiceLabel, ServiceControl}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.dummy.{DummyFactory, TraceableQueue}
import x7c1.linen.repository.loader.crawling.{Implicits, SourceInspector}
import x7c1.linen.repository.loader.schedule.LoaderSchedule
import x7c1.linen.repository.preset.PresetFactory
import x7c1.linen.repository.source.setting.{SettingSource, SettingSourceAccessorFactory}
import x7c1.wheat.macros.intent.{IntentFactory, ExtraNotFound, IntentExpander}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.patch.TaskAsync.async

import scala.concurrent.Future

class UpdaterMethods(
  service: Service with ServiceControl,
  helper: DatabaseHelper,
  queue: TraceableQueue,
  startId: Int){

  import Implicits._

  def execute(intent: Intent) = IntentExpander findFrom intent match {
    case Left(e: ExtraNotFound) => Log error e.toString
    case Left(notFound) => Log info notFound.toString
    case Right(f) => f.apply()
  }
  def createDummies(max: Int): Unit = async {
    Log info "[init]"
    val notifier = new UpdaterServiceNotifier(service, max, Date.current(), startId)
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
  def setupLoaderSchedule(accountId: Long): Unit = {
    Log error s"[init]"

    helper.selectorOf[LoaderSchedule].findPresetSchedule(accountId) map { schedule =>
      schedule.startRanges.toSeq.foreach { range =>
        Log info "h:" + range.from.hour.value
        Log info "m:" + range.from.minute.value

        val manager = service.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

        val pendingIntent = {
          val intent = IntentFactory.using[UpdaterMethods].
            create(service, service getClassOf ServiceLabel.Updater){
              _.loadFromSchedule(schedule.scheduleId)
            }

          PendingIntent.getService(service, 0, intent, 0)
        }
        val msec = {
          val calendar = Calendar.getInstance()
          calendar setTimeInMillis System.currentTimeMillis()
          calendar.add(Calendar.SECOND, 10)
          calendar.getTimeInMillis
        }
        manager.set(AlarmManager.RTC_WAKEUP, msec, pendingIntent)
      }
    }
  }
  def loadFromSchedule(scheduleId: Long): Unit = {
    Log info s"schedule:$scheduleId"
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
    val inspector = SourceInspector(helper)
    val accessor = factory.create(channelId)

    val settingSources = (0 to accessor.length - 1) flatMap accessor.findAt

    val inspectedSources = settingSources map inspector.inspectSource[SettingSource]
    val targetSources = inspectedSources collect {
      case Right(sources) => sources
    }
    inspectedSources collect {
      case Left(error) => Log error error.message
    }
    val end = targetSources.length
    val notifier = new UpdaterServiceNotifier(service, end, Date.current(), startId)

    targetSources.view map
      queue.enqueueSource foreach onUpdated(notifier, end)
  }

  private def onUpdated[A](notifier: UpdaterServiceNotifier, end: Int): Future[A] => Unit = {
    var progress = 0

    _ onComplete { _ =>
      synchronized {
        progress = progress + 1
        Log info s"[progress] $progress"
        notifier notifyProgress progress

        if (progress == end){
          Log info s"[done] hoge"
          notifier.notifyDone()
          service stopSelf startId
        }
      }
    }

  }
}
