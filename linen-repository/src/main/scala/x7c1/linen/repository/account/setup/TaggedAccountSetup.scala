package x7c1.linen.repository.account.setup

import android.database.SQLException
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{AccountParts, AccountTagLabel, AccountTagMapParts, account_tags}
import x7c1.linen.repository.account.AccountIdentifiable
import x7c1.linen.repository.date.Date
import x7c1.wheat.modern.either.{OptionEither, OptionRight}

import x7c1.wheat.modern.database.{WritableDatabase, ZeroAritySingle}


class TaggedAccountSetup[A <: AccountIdentifiable : ZeroAritySingle](
  helper: DatabaseHelper,
  tagLabel: AccountTagLabel ) {

  import x7c1.wheat.modern.either.Imports._

  private val finder = new AccountTagFinder(helper)

  def findOrCreate(parts: AccountParts): OptionEither[SQLException, Long] = {
    helper.readable.find[A]().option flatMap {
      case Some(account) => OptionRight(account.accountId)
      case None => for {
        tagId <- finder.findId(tagLabel)
        accountId <- bindTagWithAccount(tagId, parts).toOptionEither
      } yield accountId
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
