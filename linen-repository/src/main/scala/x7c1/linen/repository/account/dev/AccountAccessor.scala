package x7c1.linen.repository.account.dev

import android.database.sqlite.SQLiteDatabase

trait AccountAccessor {
  def findAt(position: Int): Option[Account]
}

object AccountAccessor {
  def create(db: SQLiteDatabase): AccountAccessor = {
    new AccountAccessorImpl(db)
  }
  def findCurrentAccountId(db: SQLiteDatabase): Option[Long] = {
    AccountAccessor.create(db).findAt(0).map(_.accountId)
  }

  private class AccountAccessorImpl(db: SQLiteDatabase) extends AccountAccessor {
    private lazy val cursor = {
      val sql =
        """SELECT
          | _id as account_id
          |FROM accounts
          |ORDER BY _id
        """.stripMargin

      db.rawQuery(sql, Array())
    }
    private lazy val idIndex = {
      cursor getColumnIndex "account_id"
    }
    override def findAt(position: Int): Option[Account] =
      cursor moveToPosition position match {
        case true => Some apply Account(
          accountId = cursor getLong idIndex
        )
        case _ => None
      }
  }
}

case class Account (accountId: Long)