package x7c1.linen.modern.init.unread

import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.accessor.unread.ChannelLoaderEvent.Done
import x7c1.linen.modern.accessor.unread.ChannelSelectable
import x7c1.wheat.macros.logger.Log


class UnreadChannelsReader(
  client: => Option[ClientAccount],
  loader: => AccessorLoader,
  onLoaded: OnAccessorsLoadedListener ) {

  def onMenuLoaded(e: Done): Unit = {
    e.headChannel match {
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
        loader.startLoading(account, channel)(onLoaded.onLoad[A])
      case None =>
        Log error s"account not found"
    }
  }
  def reloadChannel[A: ChannelSelectable](channel: A): Unit = {
    client match {
      case Some(account) =>
        Log info s"[start]"
        loader.restartLoading(account, channel)(onLoaded.onLoad[A])
      case None =>
        Log error s"account not found"
    }
  }
}
