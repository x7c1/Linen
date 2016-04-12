package x7c1.linen.database

import android.content.ContentValues
import x7c1.linen.domain.Date


object AccountRecord {
}

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
