package x7c1.linen.repository.channel.unread.selector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.channel.unread.UnreadChannel
import x7c1.wheat.modern.database.selector.SelectorProvidable.CanReify
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, ClosableSequenceLoader, TraverseOn}

class UnreadChannelSelector(protected val db: SQLiteDatabase)
  extends TraverseOn[HasAccountId, UnreadChannel]

object UnreadChannelSelector {

  type UnreadChannelLoader = ClosableSequenceLoader[HasAccountId, UnreadChannel]

  def createLoader(helper: DatabaseHelper): UnreadChannelLoader = {
    ClosableSequenceLoader[HasAccountId, UnreadChannel](helper.getReadableDatabase)
  }
  implicit def reify: CanReify[UnreadChannelSelector] = new UnreadChannelSelector(_)
}

private[unread] class CanTraverseImpl extends CanTraverse[HasAccountId, UnreadChannel]{
  override def extract[A: HasAccountId](db: SQLiteDatabase, account: A) = {
    val either = db.selectorOf[SubscribedChannel].traverseOn(account).right
    either map (_ map toUnreadChannel)
  }
  private def toUnreadChannel(channel: SubscribedChannel): UnreadChannel = {
    UnreadChannel(
      channelId = channel.channelId,
      name = channel.name
    )
  }
}
