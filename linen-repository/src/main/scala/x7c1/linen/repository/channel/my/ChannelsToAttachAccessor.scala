package x7c1.linen.repository.channel.my

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.ChannelsToAttachRecord
import x7c1.linen.database.mixin.ChannelsToAttachRecord.HasKey
import x7c1.wheat.modern.database.selector.presets.CanTraverseEntity
import x7c1.wheat.modern.database.selector.{CursorConvertible, SelectorProvidable}
import x7c1.wheat.modern.sequence.Sequence

import scala.collection.immutable.IndexedSeq

trait ChannelsToAttachAccessor extends Sequence[ChannelToAttach]{

  def collectAttached: IndexedSeq[Long] = 0 until length flatMap findAt collect {
    case x if x.isAttached => x.channelId
  }
  def collectDetached: IndexedSeq[Long] = 0 until length flatMap findAt collect {
    case x if ! x.isAttached => x.channelId
  }
  def collectAll: IndexedSeq[Long] = 0 until length flatMap findAt map (_.channelId)
}

private class ChannelsToAttachImpl(xs: Sequence[ChannelToAttach]) extends ChannelsToAttachAccessor {
  override def length = xs.length
  override def findAt(position: Int) = xs findAt position
}

class ChannelToAttach (
  val channelId: Long,
  val channelName: String,
  val isAttached: Boolean
)

object ChannelToAttach {
  implicit object convert extends CursorConvertible[ChannelsToAttachRecord, ChannelToAttach]{
    override def fromCursor = cursor =>
      new ChannelToAttach(
        channelId = cursor._id,
        channelName = cursor.name,
        isAttached = cursor.attached_source_id.nonEmpty
      )
  }
  implicit object traverse extends CanTraverseEntity[HasKey, ChannelsToAttachRecord, ChannelToAttach]

  implicit object providable extends SelectorProvidable[ChannelToAttach, ChannelToAttachSelector]
}

class ChannelToAttachSelector(db: SQLiteDatabase){
  def traverseOn[A: HasKey](key: A): Either[SQLException, ChannelsToAttachAccessor] = {
    val i = implicitly[CanTraverseEntity[HasKey, ChannelsToAttachRecord, ChannelToAttach]]
    i.extract(db, key).right map { new ChannelsToAttachImpl(_) }
  }
}
