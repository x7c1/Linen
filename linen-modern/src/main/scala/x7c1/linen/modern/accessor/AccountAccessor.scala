package x7c1.linen.modern.accessor

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.Date

trait AccountAccessor {
  def findAt(position: Int): Option[Account]
}

object AccountAccessor {
  def create(db: SQLiteDatabase): AccountAccessor = {
    new AccountAccessorImpl(db)
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

case class AccountParts(
  nickname: String,
  biography: String,
  createdAt: Date
)
object AccountParts {
  implicit object insertable extends Insertable[AccountParts]{
    override def tableName: String = "accounts"
    override def toContentValues(target: AccountParts): ContentValues = {
      val values = new ContentValues()
      values.put("nickname", target.nickname)
      values.put("biography", target.biography)
      values.put("created_at", target.createdAt.timestamp: java.lang.Integer)
      values
    }
  }
}
