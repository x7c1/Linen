package x7c1.linen.modern.accessor

import android.database.sqlite.SQLiteDatabase

object AccountAccessor {
  def create(db: SQLiteDatabase): AccountAccessor = {
    new AccountAccessorImpl(db)
  }
  private class AccountAccessorImpl(db: SQLiteDatabase) extends AccountAccessor {
    override def findFirstId(): Option[Long] = {
      val cursor = db.rawQuery("SELECT * FROM accounts ORDER BY _id LIMIT 1", Array())
      if (cursor.moveToFirst()){
        val idIndex = cursor getColumnIndex "_id"
        Some(cursor getLong idIndex)
      } else {
        None
      }
    }
  }
}

trait AccountAccessor {
  def findFirstId(): Option[Long]
}
