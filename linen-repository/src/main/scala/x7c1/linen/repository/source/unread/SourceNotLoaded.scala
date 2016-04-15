package x7c1.linen.repository.source.unread

sealed trait SourceNotLoaded {
  def message: String
}

object SourceNotLoaded {

  sealed trait NormalEmpty extends SourceNotLoaded

  sealed trait ErrorEmpty extends SourceNotLoaded

  case class ChannelNotFound(accountId: Long) extends NormalEmpty {
    override def message: String = s"channel not added (account:$accountId)"
  }
  case object AccountNotFound extends ErrorEmpty {
    override def message: String = "account not found"
  }
  case class Abort[A <: Throwable](origin: A) extends ErrorEmpty {
    override def message: String = origin.getMessage
  }
}
