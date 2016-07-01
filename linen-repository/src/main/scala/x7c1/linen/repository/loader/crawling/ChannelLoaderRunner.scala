package x7c1.linen.repository.loader.crawling

import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.NotificationKey.ChannelLoaderKey
import x7c1.linen.database.struct.{ChannelRecord, HasAccountId, HasChannelId}
import x7c1.linen.repository.loader.crawling.ChannelLoaderRunner.{AllSourcesLoaded, ChannelLoaderError, ChannelNotFound, ChannelSourceLoaded, UnexpectedError}
import x7c1.linen.repository.loader.crawling.QueueingEvent.{OnDone, OnProgress}
import x7c1.linen.repository.notification.{NotificationIdStore, ProgressNotifier}
import x7c1.wheat.calendar.CalendarDate
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class ChannelLoaderRunner private (
  context: Context,
  helper: DatabaseHelper,
  queue: TraceableQueue,
  listener: OnChannelLoaderListener ){

  def startLoading[A: HasAccountId, B: HasChannelId](account: A, channel: B): Unit = {
    val either = for {
      record <- findChannelRecord(channel).right
      notifier <- findNotifier(account, channel).right
    } yield (_: QueueingEvent) match {
      case event: OnProgress =>
        listener onProgress ChannelSourceLoaded(
          notifier = notifier,
          channelName = record.name,
          max = event.max,
          current = event.current
        )
      case event: OnDone =>
        listener onComplete AllSourcesLoaded(
          notifier = notifier,
          channelName = record.name,
          max = event.max
        )
    }
    either match {
      case Right(callback) =>
        val queueing = ChannelLoaderQueueing(helper, queue)
        queueing.start(account, channel){ callback }
      case Left(error) =>
        listener onError error
    }
  }
  private def findNotifier[A: HasAccountId, B: HasChannelId](account: A, channel: B) = {
    val key = ChannelLoaderKey(account, channel)
    NotificationIdStore(helper).getOrCreate(key) match {
      case Right(id) =>
        Right apply ProgressNotifier(
          context = context,
          startTime = CalendarDate.now(),
          notificationId = id
        )
      case Left(e) =>
        Left(UnexpectedError(e.detail))
    }
  }
  private def findChannelRecord[A: HasChannelId](channel: A) = {
    helper.selectorOf[ChannelRecord] findBy channel matches {
      case Right(Some(record)) =>
        Right(record)
      case Right(None) =>
        Left(ChannelNotFound(channel))
      case Left(e) =>
        Left(UnexpectedError(format(e.getCause){"[failed]"}))
    }
  }
}

object ChannelLoaderRunner {
  def apply(
    context: Context,
    helper: DatabaseHelper,
    queue: TraceableQueue,
    listener: OnChannelLoaderListener): ChannelLoaderRunner = {

    new ChannelLoaderRunner(context, helper, queue, listener)
  }
  case class ChannelSourceLoaded(
    notifier: ProgressNotifier,
    channelName: String,
    max: Int,
    current: Int
  )
  case class AllSourcesLoaded(
    notifier: ProgressNotifier,
    channelName: String,
    max: Int
  )
  sealed trait ChannelLoaderError {
    def detail: String
  }
  case class ChannelNotFound[A: HasChannelId](channel: A) extends ChannelLoaderError {
    override def detail = {
      val channelId = implicitly[HasChannelId[A]] toId channel
      s"channel(id:$channelId) not found"
    }
  }
  case class UnexpectedError(detail: String) extends ChannelLoaderError
}

trait OnChannelLoaderListener {
  def onProgress(event: ChannelSourceLoaded): Unit
  def onComplete(event: AllSourcesLoaded): Unit
  def onError(error: ChannelLoaderError): Unit
}
