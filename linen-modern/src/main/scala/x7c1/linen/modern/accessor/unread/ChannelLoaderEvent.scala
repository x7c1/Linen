package x7c1.linen.modern.accessor.unread

import x7c1.linen.modern.accessor.preset.ClientAccount

sealed trait ChannelLoaderEvent

object ChannelLoaderEvent {
  class Done(
    val client: ClientAccount,
    val firstChannelId: Option[Long] ) extends ChannelLoaderEvent

  class AccessorError(error: ChannelAccessorError) extends ChannelLoaderEvent {
    def detail: String = error.detail
  }
}

