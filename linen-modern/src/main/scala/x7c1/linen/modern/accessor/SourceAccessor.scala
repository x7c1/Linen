package x7c1.linen.modern.accessor

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.Source

trait SourceAccessor {

  def findAt(position: Int): Option[Source]

  def length: Int

  def positionOf(sourceId: Long): Option[Int]
}

private class SourceAccessorImpl(cursor: Cursor) extends SourceAccessor {

  private lazy val idIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"
  private lazy val descriptionIndex = cursor getColumnIndex "description"

  override def findAt(position: Int) = synchronized {
    if (cursor moveToPosition position){
      Some apply Source(
        id = cursor.getInt(idIndex),
        url = "dummy",
        title = cursor.getString(titleIndex),
        description = cursor.getString(descriptionIndex)
      )
    } else None
  }
  override def length = {
    cursor.getCount
  }
  override def positionOf(sourceId: Long): Option[Int] = {
    (0 to length - 1) find { n =>
      findAt(n).exists(_.id == sourceId)
    }
  }

}

object SourceAccessor {
  def create(context: Context): SourceAccessor = {
    val db = new LinenOpenHelper(context).getReadableDatabase
    val listId = findFirstListId(db)
    val accountId = findFirstAccountId(db)
    val cursor = createCursor(db, listId, accountId)
    new SourceAccessorImpl(cursor)
  }
  def findFirstListId(db: SQLiteDatabase) = {
    val cursor = db.rawQuery("SELECT * FROM lists LIMIT 1", Array())
    cursor.moveToFirst()
    val idIndex = cursor getColumnIndex "_id"
    cursor getLong idIndex
  }
  def findFirstAccountId(db: SQLiteDatabase) = {
    val cursor = db.rawQuery("SELECT * FROM accounts ORDER BY _id LIMIT 1", Array())
    cursor.moveToFirst()
    val idIndex = cursor getColumnIndex "_id"
    cursor getLong idIndex
  }
  def createCursor(db: SQLiteDatabase, listId: Long, accountId: Long) = {
    val sql1 =
      """SELECT
        |   source_id,
        |   Max(_id) as latest_entry_id
        |FROM entries
        |GROUP BY source_id
      """.stripMargin

    val sql2 =
      """SELECT
        |  s1.source_id,
        |  s1.list_id,
        |  s2.last_entry_id,
        |  s2.rating,
        |  s2.account_id
        |FROM list_source_map AS s1
        |LEFT JOIN source_statuses AS s2 ON s1.source_id = s2.source_id
        |WHERE s1.list_id = ? AND s2.account_id = ?
      """.stripMargin


    val sql3 =
      s"""SELECT
        |   t1.source_id AS source_id,
        |   t1.rating AS rating,
        |   IFNULL(t1.last_entry_id, 0) AS marked_entry_id,
        |   t2.latest_entry_id AS latest_entry_id
        |FROM ($sql2) AS t1
        |INNER JOIN ($sql1) AS t2 ON t1.source_id = t2.source_id
        |WHERE t2.latest_entry_id > marked_entry_id
      """.stripMargin

    val sql4 =
      s"""SELECT
         |  source_id,
         |  rating,
         |  title,
         |  description,
         |  latest_entry_id,
         |  marked_entry_id
         |FROM ($sql3) as u1
         |INNER JOIN sources as u2 ON u1.source_id = u2._id
         |ORDER BY rating DESC, source_id DESC
       """.stripMargin

    db.rawQuery(sql4, Array(listId.toString, accountId.toString))

  }
}
