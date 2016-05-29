package x7c1.linen.scene.loader.crawling

import android.app.Service
import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.ChannelRecord
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.loader.crawling.QueueingEvent.{OnDone, OnProgress}
import x7c1.linen.repository.loader.crawling.{ChannelLoaderQueueing, SourceInspector, TraceableQueue}
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.either.OptionRight
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

    val OptionRight(Some(channel)) = helper.selectorOf[ChannelRecord] findBy channelId
    val notifier = new ChannelNotifier(
      service = service,
      startTime = Date.current(),
      channelName = channel.name,
      notificationId = startId
    )
    ChannelLoaderQueueing(helper, queue).start(accountId, channelId){
      case OnProgress(current, max) =>
        Log info s"[progress] $current ${channel.name}"
        notifier.notifyProgress(current, max)
      case OnDone(max) =>
        Log info s"[done] $max ${channel.name}"
        notifier.notifyDone()
    }
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
