package x7c1.linen.scene.loader.crawling

import android.app.Service
import android.content.{Context, Intent}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.loader.crawling.ChannelLoaderRunner.{AllSourcesLoaded, ChannelLoaderError, ChannelSourceLoaded}
import x7c1.linen.repository.loader.crawling.{ChannelLoaderRunner, OnChannelLoaderListener, SourceInspector, TraceableQueue}
import x7c1.linen.repository.notification.ProgressContent
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

import scala.concurrent.Future

trait QueueingService {
  def loadSource(sourceId: Long): Unit
  def loadChannelSources(channelId: Long, accountId: Long): Unit
  def loadSubscribedChannels(accountId: Long): Unit
}

object QueueingService {
  def apply(context: Context with ServiceControl) =
    ServiceCaller.reify[QueueingService](
      context,
      context getClassOf ServiceLabel.Updater
    )

  def reify(
    service: Service with ServiceControl,
    helper: DatabaseHelper,
    queue: TraceableQueue,
    startId: Int): QueueingService = {

    new QueueingServiceImpl(service, helper, queue, startId)
  }
}

private class QueueingServiceImpl(
  service: Service with ServiceControl,
  helper: DatabaseHelper,
  queue: TraceableQueue,
  startId: Int ) extends QueueingService {

  import x7c1.linen.repository.loader.crawling.Implicits._

  override def loadSource(sourceId: Long) = {
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
  override def loadChannelSources(channelId: Long, accountId: Long) = Future {
    Log info s"[init] channel:$channelId"

    val runner = ChannelLoaderRunner(
      context = service,
      helper = helper,
      queue = queue,
      listener = new OnChannelLoader(
        intent = new Intent(service, service getClassOf ServiceLabel.Updater)
      )
    )
    runner.startLoading(
      account = accountId,
      channel = channelId
    )
  } onFailure {
    case e => Log error format(e){"[abort]"}
  }
  override def loadSubscribedChannels(accountId: Long) = Future {
    Log info s"[init]"
    helper.selectorOf[SubscribedChannel] traverseOn accountId match {
      case Left(e) => Log error format(e){"[failed]"}
      case Right(sequence) =>
        sequence.toSeq foreach { channel =>
          Log info s"$channel"
          QueueingService(service).loadChannelSources(channel.channelId, accountId)
        }
        sequence.closeCursor()
    }
  } onFailure {
    case e => Log error format(e){"[abort]"}
  }
}

private class OnChannelLoader(intent: Intent) extends OnChannelLoaderListener {
  override def onProgress(event: ChannelSourceLoaded) = {
    event.notifier show ProgressContent(
      title = s"Channel : ${event.channelName}",
      text = s"${event.current}/${event.max} loaded",
      max = event.max,
      progress = event.current,
      intent = intent
    )
  }
  override def onComplete(event: AllSourcesLoaded) = {
    event.notifier show ProgressContent(
      title = s"Channel : ${event.channelName}",
      text = s"${event.max}/${event.max} done",
      max = event.max,
      progress = event.max,
      intent = intent
    )
    Log info s"[done] ${event.channelName}"
  }
  override def onError(error: ChannelLoaderError) = {
    Log error error.detail
  }
}
