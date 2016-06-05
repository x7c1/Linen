package x7c1.linen.modern.init.unread

import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.channel.unread.{ChannelSelectable, UnreadChannel}
import x7c1.linen.repository.unread.AccessorLoader
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.Done


class UnreadChannelsReader(
  client: => Option[ClientAccount],
  loader: => AccessorLoader,
  onLoaded: OnAccessorsLoadedListener ) {

  def onMenuLoaded(e: Done[UnreadChannel]): Unit = {
    e.sequence.findAt(0) match {
      case Some(channel) =>
        loadChannel(channel)
      case None =>
        Log info "no channels"
    }
  }
  def loadChannel[A: ChannelSelectable](channel: A): Unit = {
    client match {
      case Some(account) =>
        Log info s"[start]"
        loader.reload(account, channel)(onLoaded.afterLoad[A])
      case None =>
        Log error s"account not found"
    }
  }
  def reloadChannel[A: ChannelSelectable](channel: A): Unit = {
    client match {
      case Some(account) =>
        Log info s"[start]"
        loader.reload(account, channel)(onLoaded.afterReload[A])
      case None =>
        Log error s"account not found"
    }
  }
}
