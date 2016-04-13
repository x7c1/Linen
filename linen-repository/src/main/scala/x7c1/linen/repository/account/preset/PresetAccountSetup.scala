package x7c1.linen.repository.account.preset

import x7c1.linen.database.{AccountParts, AccountTagLabel, AccountTagMapParts, ChannelParts, ClientLabel, LinenOpenHelper, PresetLabel, WritableDatabase, ZeroAritySingle, account_tags}
import x7c1.linen.domain.Date
import x7c1.linen.domain.account.{AccountIdentifiable, ClientAccount, PresetAccount}
import x7c1.linen.repository.channel.preset.{PresetChannelPiece, PresetChannel}
import x7c1.linen.repository.preset.{UnexpectedException, NoPresetTag, PresetRecordError}

object PresetAccountSetup {
  def apply(helper: LinenOpenHelper): PresetAccountSetup = {
    new PresetAccountSetup(helper)
  }
}

class PresetChannelSetup private (helper: LinenOpenHelper, account: PresetAccount){
  def getOrCreate(piece: PresetChannelPiece): Either[PresetRecordError, PresetChannel] = {
    helper.readable.find[PresetChannel] by (account -> piece) match {
      case Right(Some(x)) => Right(x)
      case Right(None) => createPresetChannel(account, piece)
      case Left(error) => Left(UnexpectedException(error))
    }
  }
  private def createPresetChannel(
    account: PresetAccount,
    piece: PresetChannelPiece): Either[PresetRecordError, PresetChannel] =

    WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
      val factory = new PresetChannelFactory(writable, account)
      for {
        channelId <- factory.insertChannel(piece).right
      } yield PresetChannel (
        channelId = channelId,
        accountId = account.accountId,
        name = piece.name
      )
    }
}
object PresetChannelSetup {
  def apply(helper: LinenOpenHelper, account: PresetAccount): PresetChannelSetup = {
    new PresetChannelSetup(helper, account)
  }
}

class PresetAccountSetup (helper: LinenOpenHelper) {
  private lazy val setup = new TaggedAccountSetup[PresetAccount](helper, PresetLabel)

  def findOrCreate(): Either[PresetRecordError, PresetAccount] = {
    val either = setup findOrCreate AccountParts(
      nickname = "Preset User",
      biography = "preset channels",
      createdAt = Date.current()
    )
    either.right map PresetAccount.apply
  }
}

object ClientAccountSetup {
  def apply(helper: LinenOpenHelper): ClientAccountSetup = {
    new ClientAccountSetup(helper)
  }
}

class ClientAccountSetup private (helper: LinenOpenHelper){
  private lazy val setup = new TaggedAccountSetup[ClientAccount](helper, ClientLabel)

  def findOrCreate(): Either[PresetRecordError, ClientAccount] = {
    val either = setup findOrCreate AccountParts(
      nickname = "no name",
      biography = "no profile",
      createdAt = Date.current()
    )
    either.right map ClientAccount.apply
  }
}

class AccountTagFinder(helper: LinenOpenHelper){
  def findId(tag: AccountTagLabel): Either[PresetRecordError, Long] =
    helper.readable.find[account_tags] by tag match {
      case Left(a) => Left(UnexpectedException(a))
      case Right(None) => Left(NoPresetTag())
      case Right(Some(record)) => Right(record.account_tag_id)
    }
}

class TaggedAccountSetup[A <: AccountIdentifiable : ZeroAritySingle](
  helper: LinenOpenHelper,
  tagLabel: AccountTagLabel ){

  private val finder = new AccountTagFinder(helper)

  def findOrCreate(parts: AccountParts): Either[PresetRecordError, Long] = {
    helper.readable.find[A]() match {
      case Right(Some(account)) => Right(account.accountId)
      case Right(None) =>
        for {
          tagId <- finder.findId(tagLabel).right
          accountId <- bindTagWithAccount(tagId, parts).right
        } yield {
          accountId
        }
      case Left(error) => Left(UnexpectedException(error))
    }
  }
  private def bindTagWithAccount(tagId: Long, parts: AccountParts): Either[PresetRecordError, Long] =
    WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
      val factory = new TaggedAccountFactory(writable)
      for {
        accountId <- factory.insertAccount(parts).right
        _ <- factory.insertTagMap(accountId, tagId).right
      } yield {
        accountId
      }
    }

}

class TaggedAccountFactory(writable: WritableDatabase){
  def insertAccount(parts: AccountParts): Either[PresetRecordError, Long] = {
    val either = writable insert parts
    either.left map UnexpectedException
  }
  def insertTagMap(accountId: Long, tagId: Long): Either[PresetRecordError, Long] = {
    val either = writable insert AccountTagMapParts(
      accountId = accountId,
      accountTagId = tagId,
      createdAt = Date.current()
    )
    either.left map UnexpectedException
  }
}

class PresetChannelFactory(writable: WritableDatabase, account: PresetAccount){
  def insertChannel(piece: PresetChannelPiece): Either[PresetRecordError, Long] = {
    val either = writable insert ChannelParts(
      accountId = account.accountId,
      name = piece.name,
      description = piece.description,
      createdAt = Date.current()
    )
    either.left map UnexpectedException
  }
}
