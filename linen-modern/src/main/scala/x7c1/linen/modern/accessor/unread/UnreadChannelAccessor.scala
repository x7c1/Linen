package x7c1.linen.modern.accessor.unread

import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.accessor.{LinenOpenHelper, Query}

trait UnreadChannelAccessor {
  def findAt(position: Int): Option[UnreadChannel]
  def length: Int
}

object UnreadChannelAccessor {
  def create(
    clientAccountId: Long,
    helper: LinenOpenHelper): Either[UnreadChannelAccessorError, UnreadChannelAccessor] = {

    ???
  }
  def createQuery(clientAccountId: Long): Query = {
    val sql = ""
    new Query(sql)
  }
}

private class UnreadChannelAccessorImpl() extends UnreadChannelAccessor {
  override def findAt(position: Int): Option[UnreadChannel] = ???
  override def length: Int = ???
}

case class UnreadChannel(
  channelId: Long,
  name: String
)

class UnreadChannelLoader(helper: LinenOpenHelper, account: ClientAccount){
  def startLoading(callback: ChannelLoadedEvent => Unit): Unit = {
    ???
  }
  lazy val accessor: UnreadChannelAccessor = {
    ???
  }
}

case class ChannelLoadedEvent()

sealed trait UnreadChannelAccessorError
