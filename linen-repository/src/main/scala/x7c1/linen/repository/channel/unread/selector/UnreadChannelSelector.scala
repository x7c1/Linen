package x7c1.linen.repository.channel.unread.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.channel.unread.UnreadChannel
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.ClosableSequence

class UnreadChannelSelector(protected val db: SQLiteDatabase)
  extends TraverseOn

trait TraverseOn {
  protected def db: SQLiteDatabase

  type Traversed = Either[SQLException, ClosableSequence[UnreadChannel]]

  def traverseOn[A: HasAccountId](account: A): Traversed = {
    // TODO: select only unread channels
    db.selectorOf[SubscribedChannel].traverseOn(account).right map (_ map toUnreadChannel)
  }
  private def toUnreadChannel(channel: SubscribedChannel): UnreadChannel = {
    UnreadChannel(
      channelId = channel.channelId,
      name = channel.name
    )
  }
}
