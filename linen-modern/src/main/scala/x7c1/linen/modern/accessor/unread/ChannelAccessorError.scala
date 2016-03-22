package x7c1.linen.modern.accessor.unread

import x7c1.linen.modern.init.updater.ThrowableFormatter

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
