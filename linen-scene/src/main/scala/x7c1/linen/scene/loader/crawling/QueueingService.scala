package x7c1.linen.scene.loader.crawling

import android.app.Service
import android.content.{Context, Intent}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.loader.crawling.ChannelLoaderRunner.{AllSourcesLoaded, ChannelLoaderError, ChannelSourceLoaded}
import x7c1.linen.repository.loader.crawling.PresetLoaderRunner.{AllChannelsLoaded, ChannelLoaded, PresetLoaderError}
import x7c1.linen.repository.loader.crawling.{ChannelLoaderRunner, CrawlerContext, CrawlerFate, OnChannelLoaderListener, OnPresetLoaderListener, PresetLoaderRunner, SourceInspector, TraceableQueue}
import x7c1.linen.repository.notification.ProgressContent
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.fate.FutureFate
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

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
    queue: TraceableQueue): QueueingService = {

    new QueueingServiceImpl(service, helper, queue)
  }
}

private class QueueingServiceImpl(
  service: Service with ServiceControl,
  helper: DatabaseHelper,
  queue: TraceableQueue) extends QueueingService {

  override def loadSource(sourceId: Long) = {
    val inspector = SourceInspector(helper)
    val fate = FutureFate.on[CrawlerContext] create {
      inspector inspectSource sourceId
    } map {
      source =>
        queue enqueueSource source run CrawlerContext atLeft {
          Log error _.detail
        }
    }
    fate run CrawlerContext atLeft {
      e =>
        Log error s"source(id:$sourceId): ${e.message}"
    }
  }

  override def loadChannelSources(channelId: Long, accountId: Long) = CrawlerFate run {
    Log info s"[init] channel:$channelId"

    val runner = ChannelLoaderRunner(
      context = service,
      helper = helper,
      queue = queue,
      listener = new OnChannelLoader(
        intent = new Intent(service, service getClassOf ServiceLabel.Updater)
      )
    )
    runner startLoading accountId -> channelId
  } atLeft {
    e => Log error format(e.cause) {
      "[abort]"
    }
  }

  override def loadSubscribedChannels(accountId: Long) = CrawlerFate run {
    Log info s"[init] account:$accountId"

    val intent = new Intent(service, service getClassOf ServiceLabel.Updater)
    val runner = PresetLoaderRunner(
      context = service,
      helper = helper,
      queue = queue,
      presetLoaderListener = new OnPresetLoader(intent),
      channelLoaderListener = new OnPresetChannelLoader(intent)
    )
    runner.startLoading(accountId)
  } atLeft {
    e => Log error format(e.cause) {
      "[abort]"
    }
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

private class OnPresetLoader(intent: Intent) extends OnPresetLoaderListener {
  override def onProgress(event: ChannelLoaded) = {
    event.notifier show ProgressContent(
      title = "Loading channels..",
      text = s"${event.current}/${event.max} channels loaded.",
      max = event.max,
      progress = event.current,
      intent = intent
    )
  }

  override def onError(error: PresetLoaderError) = {
    Log error error.detail
  }

  override def onComplete(event: AllChannelsLoaded) = {
    event.notifier show ProgressContent(
      title = "Loading completed",
      text = s"${event.max} channels loaded.",
      max = event.max,
      progress = event.max,
      intent = intent
    )
    Log info s"[done]"
  }
}

private class OnPresetChannelLoader(intent: Intent) extends OnChannelLoaderListener {
  private val base = new OnChannelLoader(intent)

  override def onProgress(event: ChannelSourceLoaded): Unit = {
    base.onProgress(event)
  }

  override def onError(error: ChannelLoaderError): Unit = {
    base.onError(error)
  }

  override def onComplete(event: AllSourcesLoaded): Unit = {
    base.onComplete(event)
    event.notifier.hide()
  }
}
