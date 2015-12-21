package x7c1.linen.modern.accessor

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.Source

trait SourceAccessor {

  def findAt(position: Int): Option[Source]

  def length: Int

  def positionOf(sourceId: Long): Option[Int]
}

class SourceAccessorHolder(accessor: SourceAccessor) extends SourceAccessor {

  override def findAt(position: Int): Option[Source] = {
    accessor findAt position
  }
  override def positionOf(sourceId: Long): Option[Int] = {
    accessor positionOf sourceId
  }
  override def length: Int = {
    accessor.length
  }
}

private class SourceAccessorImpl(cursor: Cursor) extends SourceAccessor {

  private lazy val idIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"
  private lazy val descriptionIndex = cursor getColumnIndex "description"
  private lazy val startEntryIdIndex = cursor getColumnIndex "start_entry_id"

  override def findAt(position: Int) = synchronized {
    if (cursor moveToPosition position){
      Some apply Source(
        id = cursor.getLong(idIndex),
        url = "dummy",
        title = cursor.getString(titleIndex),
        description = cursor.getString(descriptionIndex),
        startEntryId = {
          /*
            cannot use cursor.getLong here
              because it returns 0 when target value is null
           */
          Option(cursor getString startEntryIdIndex).map(_.toLong)
        }
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
  def create(db: SQLiteDatabase): SourceAccessor = {
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
        |  s2.start_entry_id
        |FROM list_source_map AS s1
        |LEFT JOIN source_statuses AS s2 ON s1.source_id = s2.source_id
        |WHERE s1.list_id = ? AND s2.account_id = ?
      """.stripMargin

    val sql3 =
      s"""SELECT
        |  t1.source_id AS source_id,
        |  t1.start_entry_id AS start_entry_id,
        |  t2.latest_entry_id AS latest_entry_id
        |FROM ($sql2) AS t1
        |INNER JOIN ($sql1) AS t2 ON t1.source_id = t2.source_id
        |WHERE t2.latest_entry_id > IFNULL(t1.start_entry_id, 0)
      """.stripMargin

    val sql4 =
      s"""SELECT
        |  u1.source_id AS source_id,
        |  u2.title AS title,
        |  u2.description AS description,
        |  u1.latest_entry_id AS latest_entry_id,
        |  u1.start_entry_id AS start_entry_id
        |FROM ($sql3) as u1
        |INNER JOIN sources as u2 ON u1.source_id = u2._id
       """.stripMargin

    val sql5 =
      s"""SELECT
        |  p4.source_id AS source_id,
        |  p4.title AS title,
        |  p4.description AS description,
        |  p4.start_entry_id AS start_entry_id,
        |  p4.latest_entry_id AS latest_entry_id,
        |  p1.rating AS rating
        |FROM source_ratings AS p1
        |INNER JOIN ($sql4) AS p4 ON p1.source_id = p4.source_id
        |WHERE p1.owner_account_id = ?
        |ORDER BY p1.rating DESC, p1.source_id DESC
       """.stripMargin

    db.rawQuery(sql5,
      Array(listId.toString, accountId.toString, accountId.toString))
  }
}
