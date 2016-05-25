package x7c1.linen.scene.loader.crawling

import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{AccountIdentifiable, LoaderScheduleLike}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class SubscribedChannelsLoader private (
  context: Context with ServiceControl,
  helper: DatabaseHelper ){

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
