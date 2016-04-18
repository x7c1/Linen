package x7c1.linen.repository.channel.my

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{ChannelSourceMapKey, ChannelSourceMapParts}
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.WritableDatabase
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class MyChannelConnection (writable: WritableDatabase, sourceId: Long){

  def attachTo(channelIds: Seq[Long]): Unit = {
    channelIds foreach { id =>
      val parts = ChannelSourceMapParts(
        channelId = id,
        sourceId = sourceId,
        createdAt = Date.current()
      )
      writable.insert(parts).left foreach { e =>
        Log error format(e){
          s"[failed] attach source:$sourceId to channel:$id"
        }
      }
    }
    Log info s"[done] $channelIds"
  }
  def detachFrom(channelIds: Seq[Long]): Unit = {
    channelIds foreach { id =>
      val key = ChannelSourceMapKey(
        channelId = id,
        sourceId = sourceId
      )
      writable.delete(key).left foreach { e =>
        Log error format(e){
          s"[failed] detach source:$sourceId from channel:$id"
        }
      }
    }
    Log info s"[done] $channelIds"
  }
}

class MyChannelConnectionUpdater (
  connection: MyChannelConnection, accessor: ChannelsToAttachAccessor){

  private def updateIds(channelIds: Seq[Long]): Unit = {

    // todo: use transaction

    connection attachTo {
      val attachedChannels = accessor.collectAttached
      channelIds diff attachedChannels
    }
    connection detachFrom {
      val unselectedChannels = accessor.collectAll diff channelIds
      val detachedChannels = accessor.collectDetached
      unselectedChannels diff detachedChannels
    }
  }
  def updateMapping(map: collection.Map[Long, Boolean]): Unit = {
    val xs = map collect { case (id, attached) if attached => id }
    this updateIds xs.toSeq
  }
}

object MyChannelConnectionUpdater {
  def apply(
    helper: DatabaseHelper,
    sourceId: Long,
    accessor: ChannelsToAttachAccessor): MyChannelConnectionUpdater = {

    new MyChannelConnectionUpdater(
      connection = new MyChannelConnection(helper.writable, sourceId),
      accessor = accessor
    )
  }
}
