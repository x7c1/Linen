package x7c1.linen.modern.accessor.preset

import x7c1.linen.modern.accessor.database.{AccountTagMapParts, Preset, account_tags}
import x7c1.linen.modern.accessor.{WritableDatabase, AccountParts, LinenOpenHelper}
import x7c1.linen.modern.struct.Date

object PresetAccountAccessor {
  def apply(helper: LinenOpenHelper): PresetAccountAccessor = {
    new PresetAccountAccessor(helper)
  }
}
class PresetAccountAccessor(helper: LinenOpenHelper){
  def setupAccountId(): Either[PresetAccountError, Long] = {
    helper.readable.find[PresetAccount].byQuery() match {
      case Right(Some(account)) => Right(account.accountId)
      case Right(None) => createPresetAccount()
      case Left(error) => Left(UnexpectedException(error))
    }
  }
  def setupChannelId(accountId: Long): Either[PresetAccountError, Long] = {
    ???
  }
  def createPresetAccount(): Either[PresetAccountError, Long] =
    WritableDatabase.transaction(helper.getWritableDatabase){ writable =>
      for {
        accountId <- insertAccount(writable).right
        tagId <- findPresetTagId.right
        _ <- insertAccountTagMap(writable, accountId, tagId).right
      } yield {
        accountId
      }
    }

  def insertAccount(writable: WritableDatabase) = {
    val either = writable insert AccountParts(
      nickname = "preset user",
      biography = "preset maker",
      createdAt = Date.current()
    )
    either.left map UnexpectedException
  }
  def findPresetTagId = helper.readable.find[account_tags](Preset) match {
    case Left(a) => Left(UnexpectedException(a))
    case Right(None) => Left(NoPresetTag())
    case Right(Some(tag)) => Right(tag.account_tag_id)
  }
  def insertAccountTagMap(writable: WritableDatabase,  accountId: Long, tagId: Long) = {
    val either = writable insert AccountTagMapParts(
      accountId = accountId,
      accountTagId = tagId,
      createdAt = Date.current()
    )
    either.left map UnexpectedException
  }
}

sealed trait PresetAccountError

case class UnexpectedException(cause: Exception) extends PresetAccountError

case class NoPresetAccount() extends PresetAccountError

case class NoPresetTag() extends PresetAccountError
