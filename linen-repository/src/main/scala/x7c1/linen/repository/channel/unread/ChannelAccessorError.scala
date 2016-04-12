package x7c1.linen.repository.channel.unread

import x7c1.wheat.modern.formatter.ThrowableFormatter

sealed trait ChannelAccessorError {
  def detail: String
}

object ChannelAccessorError {
  case class UnexpectedError(cause: Throwable) extends ChannelAccessorError {
    override def detail: String = {
      ThrowableFormatter.format(cause){ "unknown error" }
    }
  }
}
