package x7c1.linen.repository.channel.unread

import x7c1.linen.domain.account.ClientAccount

sealed trait ChannelLoaderEvent

object ChannelLoaderEvent {
  class Done(
    val client: ClientAccount,
    val headChannel: Option[UnreadChannel] ) extends ChannelLoaderEvent

  class AccessorError(error: ChannelAccessorError) extends ChannelLoaderEvent {
    def detail: String = error.detail
  }
}
