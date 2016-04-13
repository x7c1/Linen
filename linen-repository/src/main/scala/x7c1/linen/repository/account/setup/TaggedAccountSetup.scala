package x7c1.linen.repository.account.setup

import x7c1.linen.database.struct.{AccountTagLabel, AccountParts, account_tags, AccountTagMapParts}
import x7c1.linen.database.{WritableDatabase, LinenOpenHelper, ZeroAritySingle}
import x7c1.linen.repository.account.AccountIdentifiable
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.preset.{NoPresetTag, UnexpectedException, PresetRecordError}

class TaggedAccountSetup[A <: AccountIdentifiable : ZeroAritySingle](
  helper: LinenOpenHelper,
  tagLabel: AccountTagLabel ) {

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

private class AccountTagFinder(helper: LinenOpenHelper){
  def findId(tag: AccountTagLabel): Either[PresetRecordError, Long] =
    helper.readable.find[account_tags] by tag match {
      case Left(a) => Left(UnexpectedException(a))
      case Right(None) => Left(NoPresetTag())
      case Right(Some(record)) => Right(record.account_tag_id)
    }
}

private class TaggedAccountFactory(writable: WritableDatabase){
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
