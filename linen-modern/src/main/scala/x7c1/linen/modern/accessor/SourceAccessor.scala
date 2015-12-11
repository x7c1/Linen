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
    val cursor = createCursor(db)
    new SourceAccessorImpl(cursor)
  }

  def createCursor(db: SQLiteDatabase) = {
    val sql1 =
      """SELECT * FROM list_source_map
        | INNER JOIN sources ON list_source_map.source_id = sources._id
        | ORDER BY sources.rating DESC, sources._id DESC""".stripMargin

    val sql2 =
      """SELECT source_id FROM entries
        | WHERE read_state = 0
        | GROUP BY source_id """.stripMargin

    val sql3 =
      s"""SELECT
        |   s1._id as source_id,
        |   substr(s1.title, 1, 100) as title,
        |   substr(s1.description, 1, 100) as description,
        |   s1.rating as rating
        | FROM ($sql1) as s1
        | INNER JOIN ($sql2) as s2
        | ON s1.source_id = s2.source_id
        | ORDER BY s1.rating DESC, source_id DESC""".stripMargin

    db.rawQuery(sql3, Array())
  }
}
