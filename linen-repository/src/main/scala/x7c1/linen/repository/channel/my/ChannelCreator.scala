package x7c1.linen.repository.channel.my

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{AccountIdentifiable, ChannelParts}
import x7c1.linen.repository.account.AccountBase
import x7c1.linen.repository.channel.my.ChannelCreator.InputToCreate
import x7c1.linen.repository.channel.subscribe.ChannelSubscriber
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.logger.Log

class ChannelCreator[A: AccountIdentifiable] private (
  helper: DatabaseHelper, account: A){

  private val accountId = implicitly[AccountIdentifiable[A]] toId account

  def createChannel(input: InputToCreate): Either[SqlError, Long] = {
    Log info s"[init] account:$accountId, input:$input"

    def create() = helper.writable insert ChannelParts(
      accountId = accountId,
      name = input.channelName,
      description = input.description.getOrElse(""),
      createdAt = Date.current()
    )
    def subscribe(channelId: Long) = {
      val subscriber = new ChannelSubscriber(
        account = AccountBase(accountId),
        helper = helper
      )
      subscriber subscribe channelId
    }
    // todo: use transaction
    val either = for {
      channelId <- create().right
      _ <- subscribe(channelId).toEither.right
    } yield {
      channelId
    }
    either.left map SqlError
  }
}

object ChannelCreator {
  def apply[A: AccountIdentifiable](helper: DatabaseHelper, account: A): ChannelCreator[A] = {
    new ChannelCreator[A](helper, account)
  }
  case class InputToCreate(
    channelName: String,
    description: Option[String] = None
  )
}
