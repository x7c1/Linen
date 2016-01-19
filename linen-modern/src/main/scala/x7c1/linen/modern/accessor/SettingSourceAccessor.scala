package x7c1.linen.modern.accessor

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

trait SettingSourceAccessor {
  def findAt(position: Int): Option[SettingSource]
  def length: Int
}

private class SettingSourceAccessorImpl(cursor: Cursor) extends SettingSourceAccessor {

  private lazy val idIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"

  override def length: Int = cursor.getCount

  override def findAt(position: Int): Option[SettingSource] = {
    if (cursor moveToPosition position){
      Some apply SettingSource(
        sourceId = cursor getLong idIndex,
        title = cursor getString titleIndex
      )
    } else None
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
        | _id as source_id,
        | title as title
        |FROM sources
      """.stripMargin

    db.rawQuery(sql1, Array())
  }
}
