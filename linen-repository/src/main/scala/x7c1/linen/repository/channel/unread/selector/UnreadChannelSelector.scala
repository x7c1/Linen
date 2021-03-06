package x7c1.linen.repository.channel.unread.selector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.mixin.UnreadChannelRecord
import x7c1.linen.database.struct.{HasAccountId, HasChannelStatusKey}
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.channel.unread.UnreadChannel
import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, ClosableSequenceLoader, TraverseOn}
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class UnreadChannelSelector(protected val db: SQLiteDatabase)
  extends TraverseOn[HasAccountId, UnreadChannel]{

  def isUnread[A: HasChannelStatusKey](key: A): Boolean = {
    db.selectorOf[UnreadChannelRecord].detectFrom(key) match {
      case Right(detected) =>
        detected
      case Left(e) =>
        Log error format(e){"[failed]"}
        false
    }
  }
}

object UnreadChannelSelector {

  type UnreadChannelLoader = ClosableSequenceLoader[CrawlerContext, HasAccountId, UnreadChannel]

  def createLoader(helper: DatabaseHelper): UnreadChannelLoader = {
    ClosableSequenceLoader[CrawlerContext, HasAccountId, UnreadChannel](helper.getReadableDatabase)
  }
}

private[unread] class CanTraverseImpl extends CanTraverse[HasAccountId, UnreadChannel]{
  override def extract[A: HasAccountId](db: SQLiteDatabase, account: A) = {
    val isUnread = {
      db.selectorOf[UnreadChannel].isUnread[SubscribedChannel] _
    }
    db.selectorOf[SubscribedChannel].traverseOn(account).right map {
      _ filter isUnread map toUnreadChannel
    }
  }
  private def toUnreadChannel(channel: SubscribedChannel): UnreadChannel = {
    UnreadChannel(
      channelId = channel.channelId,
      name = channel.name
    )
  }
}
