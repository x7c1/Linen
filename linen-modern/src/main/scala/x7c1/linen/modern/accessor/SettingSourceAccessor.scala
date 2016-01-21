package x7c1.linen.modern.accessor

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.macros.database.TypedCursor

trait SettingSourceAccessor {
  def findAt(position: Int): Option[SettingSource]
  def length: Int
}

private class SettingSourceAccessorImpl(rawCursor: Cursor) extends SettingSourceAccessor {

  private lazy val cursor = TypedCursor[SourceRecordColumn](rawCursor)

  override def length: Int = rawCursor.getCount

  override def findAt(position: Int): Option[SettingSource] = {
    cursor.moveToFind(position){
      SettingSource(
        sourceId = cursor._id,
        title = cursor.title
      )
    }
  }
}

case class SettingSource(sourceId: Long, title: String)

class SettingSourceAccessorFactory(
  db: SQLiteDatabase,
  accountId: Long ){

  def create(channelId: Long): SettingSourceAccessor = {
    val cursor = createCursor(channelId)
    new SettingSourceAccessorImpl(cursor)
  }
  def createCursor(channelId: Long): Cursor = {
    val sql1 =
      """SELECT
        | _id,
        | title
        |FROM sources
      """.stripMargin

    db.rawQuery(sql1, Array())
  }
}
