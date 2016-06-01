package x7c1.linen.repository.channel.unread

import android.database.SQLException
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

sealed trait ChannelLoaderEvent

object ChannelLoaderEvent {
  class Done(
    val headChannel: Option[UnreadChannel] ) extends ChannelLoaderEvent

  class AccessorError(e: SQLException) extends ChannelLoaderEvent {
    def detail: String = format(e.getCause){"[failed]"}
  }
}
