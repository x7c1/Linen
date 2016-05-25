package x7c1.linen.scene.loader.crawling

import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{AccountIdentifiable, LoaderScheduleLike}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, PresetLoaderSchedule}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class SubscribedChannelsLoader private (
  context: Context with ServiceControl,
  helper: DatabaseHelper ){

  def loadFromSchedule[A: LoaderScheduleLike](schedule: A): Unit = {
    helper.selectorOf[LoaderSchedule] findBy schedule matches {
      case Right(Some(x: PresetLoaderSchedule)) =>
        execute(x.accountId)
      case Right(Some(x)) =>
        val id = implicitly[LoaderScheduleLike[A]] toId schedule
        Log error s"not implemented yet (id:$id, name:${x.name})"
      case Right(None) =>
        val id = implicitly[LoaderScheduleLike[A]] toId schedule
        Log error s"schedule not found (id:$id)"
      case Left(e) =>
        Log error format(e){"[failed]"}
    }
  }
  def execute[A: AccountIdentifiable](account: A): Unit = {
    Log info s"[init]"

    val accountId = implicitly[AccountIdentifiable[A]] toId account

    helper.selectorOf[SubscribedChannel] traverseOn account match {
      case Left(e) => Log error format(e){"[failed]"}
      case Right(sequence) =>
        sequence.toSeq foreach { channel =>
          Log info s"$channel"
          QueueingService(context).loadChannelSources(channel.channelId, accountId)
        }
        sequence.closeCursor()
    }
  }
}

object SubscribedChannelsLoader {
  def apply(
    context: Context with ServiceControl,
    helper: DatabaseHelper): SubscribedChannelsLoader = {

    new SubscribedChannelsLoader(context, helper)
  }
}
