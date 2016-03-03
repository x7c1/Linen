package x7c1.linen.modern.accessor.preset

import x7c1.linen.modern.accessor.database.{AccountTagMapParts, Preset, account_tags}
import x7c1.linen.modern.accessor.{ChannelParts, AccountParts, LinenOpenHelper, WritableDatabase}
import x7c1.linen.modern.struct.Date

object PresetAccountAccessor {
  def apply(helper: LinenOpenHelper): PresetAccountAccessor = {
    new PresetAccountAccessor(helper)
  }
}
class PresetAccountAccessor(helper: LinenOpenHelper){
  def setupAccount(): Either[PresetRecordError, PresetAccount] = {
    helper.readable.find[PresetAccount]() match {
      case Right(Some(account)) => Right(account)
      case Right(None) => createPresetAccount()
      case Left(error) => Left(UnexpectedException(error))
    }
  }
  def setupChannel(
    account: PresetAccount,
    piece: PresetChannelPiece): Either[PresetRecordError, PresetChannel] = {

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

  private def createPresetAccount(): Either[PresetRecordError, PresetAccount] =
    WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
      val factory = new PresetAccountFactory(writable)
      for {
        accountId <- factory.insertAccount().right
        tagId <- findPresetTagId.right
        _ <- factory.insertTagMap(accountId, tagId).right
      } yield {
        PresetAccount(accountId)
      }
    }

  private def findPresetTagId = helper.readable.find[account_tags] by Preset match {
    case Left(a) => Left(UnexpectedException(a))
    case Right(None) => Left(NoPresetTag())
    case Right(Some(tag)) => Right(tag.account_tag_id)
  }
}

class PresetAccountFactory(writable: WritableDatabase){
  def insertAccount(): Either[PresetRecordError, Long] = {
    val either = writable insert AccountParts(
      nickname = "Preset User",
      biography = "manage preset channels",
      createdAt = Date.current()
    )
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
