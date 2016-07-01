package x7c1.linen.repository.loader.crawling

import java.util.concurrent.atomic.AtomicInteger

import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.database.struct.NotificationKey.PresetLoaderKey
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.loader.crawling.ChannelLoaderRunner.{AllSourcesLoaded, ChannelLoaderError, ChannelSourceLoaded}
import x7c1.linen.repository.loader.crawling.PresetLoaderRunner.{AllChannelsLoaded, ChannelLoaded, PresetLoaderError, UnexpectedError}
import x7c1.linen.repository.notification.{NotificationIdStore, ProgressNotifier}
import x7c1.wheat.calendar.CalendarDate
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class PresetLoaderRunner(
  context: Context,
  helper: DatabaseHelper,
  queue: TraceableQueue,
  presetLoaderListener: OnPresetLoaderListener,
  channelLoaderListener: OnChannelLoaderListener ){

  def startLoading[A: HasAccountId](account: A): Unit = {
    val either = for {
      notifier <- findNotifier(account).right
      sequence <- findSequence(account).right
    } yield {
      val counter = new ChannelProgressCounter(
        notifier = notifier,
        channelLoaderListener = channelLoaderListener,
        presetLoaderListener = presetLoaderListener,
        max = sequence.length
      )
      sequence -> ChannelLoaderRunner(
        context = context,
        helper = helper,
        queue = queue,
        listener = counter
      )
    }
    either match {
      case Right((sequence, runner)) =>
        sequence.toSeq foreach { runner.startLoading(account, _) }
        sequence.closeCursor()
      case Left(error) =>
        presetLoaderListener onError error
    }
  }
  private def findSequence[A: HasAccountId](account: A) = {
    val either = helper.selectorOf[SubscribedChannel] traverseOn account
    either.left.map { e =>
      UnexpectedError(format(e.getCause){"[failed]"})
    }
  }
  private def findNotifier[A: HasAccountId](account: A) = {
    val key = PresetLoaderKey(account)
    NotificationIdStore(helper).getOrCreate(key) match {
      case Right(id) =>
        Right apply ProgressNotifier(
          context = context,
          startTime = CalendarDate.now(),
          notificationId = id
        )
      case Left(e) =>
        Left apply UnexpectedError(e.detail)
    }
  }
}

object PresetLoaderRunner {
  def apply(
    context: Context,
    helper: DatabaseHelper,
    queue: TraceableQueue,
    presetLoaderListener: OnPresetLoaderListener,
    channelLoaderListener: OnChannelLoaderListener): PresetLoaderRunner = {

    new PresetLoaderRunner(
      context, helper, queue,
      presetLoaderListener,
      channelLoaderListener
    )
  }
  case class ChannelLoaded(
    notifier: ProgressNotifier,
    max: Int,
    current: Int
  )
  case class AllChannelsLoaded(
    notifier: ProgressNotifier,
    max: Int
  )
  sealed trait PresetLoaderError {
    def detail: String
  }
  case class UnexpectedError(detail: String) extends PresetLoaderError
}

trait OnPresetLoaderListener {
  def onProgress(event: ChannelLoaded): Unit
  def onComplete(event: AllChannelsLoaded): Unit
  def onError(error: PresetLoaderError): Unit
}

private class ChannelProgressCounter(
  notifier: ProgressNotifier,
  channelLoaderListener: OnChannelLoaderListener,
  presetLoaderListener: OnPresetLoaderListener,
  max: Int ) extends OnChannelLoaderListener {

  private val current = new AtomicInteger(0)

  override def onProgress(event: ChannelSourceLoaded) = {
    channelLoaderListener.onProgress(event)
  }
  override def onError(error: ChannelLoaderError) = {
    channelLoaderListener.onError(error)
  }
  override def onComplete(event: AllSourcesLoaded) = {
    channelLoaderListener onComplete event
    presetLoaderListener onProgress ChannelLoaded(
      notifier = notifier,
      max = max,
      current = current.incrementAndGet()
    )
    if (current.get() == max){
      presetLoaderListener onComplete AllChannelsLoaded(
        notifier = notifier,
        max = max
      )
    }
  }
}
