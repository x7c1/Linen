package x7c1.linen.repository.account.setup

import android.database.SQLException
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{AccountParts, AccountTagLabel, AccountTagMapParts, account_tags}
import x7c1.linen.repository.account.AccountIdentifiable
import x7c1.linen.repository.date.Date

import x7c1.wheat.modern.database.{WritableDatabase, ZeroAritySingle}
import x7c1.wheat.modern.either.OptionEither


class TaggedAccountSetup[A <: AccountIdentifiable : ZeroAritySingle](
  helper: DatabaseHelper,
  tagLabel: AccountTagLabel ) {

  private val finder = new AccountTagFinder(helper)

  def findOrCreate(parts: AccountParts): Either[AccountSetupError, Long] = {
    helper.readable.find[A]() via {
      case Right(Some(account)) => Right(account.accountId)
      case Right(None) => createAccount(parts)
      case Left(e) => Left(UnexpectedException(e))
    }
  }
  private def createAccount(parts: AccountParts) = {
    finder findId tagLabel via {
      case Right(Some(tagId)) =>
        bindTagWithAccount(tagId, parts).left map (UnexpectedException(_))
      case Right(None) =>
        Left(AccountTagNotFound(tagLabel))
      case Left(e) =>
        Left(UnexpectedException(e))
    }
  }
  private def bindTagWithAccount(tagId: Long, parts: AccountParts) =
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

private class AccountTagFinder(helper: DatabaseHelper){
  def findId(tag: AccountTagLabel): OptionEither[SQLException, Long] = {
    val either = helper.readable.find[account_tags] by tag
    either map (_.account_tag_id)
  }
}

private class TaggedAccountFactory(writable: WritableDatabase){
  def insertAccount(parts: AccountParts): Either[SQLException, Long] = {
    writable insert parts
  }
  def insertTagMap(accountId: Long, tagId: Long): Either[SQLException, Long] = {
    writable insert AccountTagMapParts(
      accountId = accountId,
      accountTagId = tagId,
      createdAt = Date.current()
    )
  }
}