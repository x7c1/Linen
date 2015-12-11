package x7c1.linen.modern.accessor

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.{Date, Entry, EntryDetail, EntryOutline}

trait EntryAccessor[A <: Entry]{

  def findAt(position: Int): Option[A]

  def length: Int

  def firstEntryPositionOf(sourceId: Long): Option[Int]
}

private class EntryAccessorImpl[A <: Entry](
  factory: EntryFactory[A],
  cursor: Cursor, positionMap: Map[Long, Int]) extends EntryAccessor[A] {

  override def findAt(position: Int) = synchronized {
    if (cursor moveToPosition position){
      Some apply factory.createEntry()
    } else None
  }

  override def length = {
    cursor.getCount
  }
  override def firstEntryPositionOf(sourceId: Long): Option[Int] = {
    positionMap.get(sourceId)
  }
}

trait EntryFactory[A <: Entry]{
  def createEntry(): A
}

class EntryOutlineFactory(cursor: Cursor) extends EntryFactory[EntryOutline] {

  private lazy val entryIdIndex = cursor getColumnIndex "entry_id"
  private lazy val sourceIdIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"
  private lazy val contentIndex = cursor getColumnIndex "content"
  private lazy val createdAtIndex = cursor getColumnIndex "created_at"

  override def createEntry(): EntryOutline = {
    EntryOutline(
      entryId = cursor getInt entryIdIndex,
      sourceId = cursor getInt sourceIdIndex,
      url = "dummy",
      shortTitle = cursor getString titleIndex,
      shortContent = cursor getString contentIndex,
      createdAt = Date.dummy()
    )
  }
}

class EntryDetailFactory(cursor: Cursor) extends EntryFactory[EntryDetail] {

  private lazy val entryIdIndex = cursor getColumnIndex "entry_id"
  private lazy val sourceIdIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"
  private lazy val contentIndex = cursor getColumnIndex "content"
  private lazy val createdAtIndex = cursor getColumnIndex "created_at"

  override def createEntry(): EntryDetail = {
    EntryDetail(
      entryId = cursor getInt entryIdIndex,
      sourceId = cursor getInt sourceIdIndex,
      url = "dummy",
      fullTitle = cursor getString titleIndex,
      fullContent = cursor getString contentIndex,
      createdAt = Date.dummy()
    )
  }
}

object EntryAccessor {

  def forEntryOutline(context: Context): EntryAccessor[EntryOutline] = {
    val db = new LinenOpenHelper(context).getReadableDatabase
    val content = createSql4("substr(entries.content, 1, 100)")
    val cursor = createCursor(db, content)
    val map = createSourcePositionMap(db, content)
    val factory = new EntryOutlineFactory(cursor)

    new EntryAccessorImpl(factory, cursor, map)
  }

  def forEntryDetail(context: Context): EntryAccessor[EntryDetail] = {
    val db = new LinenOpenHelper(context).getReadableDatabase
    val content = createSql4("entries.content")
    val cursor = createCursor(db, content)
    val map = createSourcePositionMap(db, content)
    val factory = new EntryDetailFactory(cursor)

    new EntryAccessorImpl(factory, cursor, map)
  }

  val sql1 =
    s"""SELECT * FROM list_source_map
       | INNER JOIN sources ON list_source_map.source_id = sources._id
       | ORDER BY sources.rating DESC""".stripMargin

  val sql2 =
    s"""SELECT source_id FROM entries
       | WHERE read_state = 0
       | GROUP BY source_id """.stripMargin

  val sql3 =
    s"""SELECT
       |   s1._id as source_id,
       |   substr(s1.title, 1, 100) as title,
       |   s1.rating as rating,
       |   substr(s1.description, 1, 100) as description
       | FROM ($sql1) as s1
       | INNER JOIN ($sql2) as s2
       | ON s1.source_id = s2.source_id""".stripMargin

  val entries =
    s"""SELECT
       |   _id,
       |   source_id,
       |   title,
       |   content,
       |   created_at
       | FROM entries
       | ORDER BY _id DESC""".stripMargin

  def createSql4(content: String) =
    s"""SELECT
       |   entries._id as entry_id,
       |   s3.source_id as source_id,
       |   entries.title as title,
       |   $content as content,
       |   s3.rating as rating,
       |   entries.created_at as created_at
       | FROM ($sql3) as s3
       | INNER JOIN ($entries) as entries
       | ON s3.source_id = entries.source_id
       | ORDER BY rating DESC, source_id DESC, entry_id DESC""".stripMargin

  def createCursor(db: SQLiteDatabase, sql4: String) = {
    db.rawQuery(sql4, Array())
  }

  def createCounterCursor(db: SQLiteDatabase, sql4: String) = {
    val sql5 =
      s"""SELECT COUNT(entry_id) as count, source_id, rating
         | FROM($sql4)
         | GROUP BY source_id, rating
         | ORDER BY rating DESC, source_id DESC
       """.stripMargin

    db.rawQuery(sql5, Array())
  }

  def createSourcePositionMap(db: SQLiteDatabase, sql4: String): Map[Long, Int] = {
    val cursor = createCounterCursor(db, sql4)
    val countIndex = cursor getColumnIndex "count"
    val sourceIdIndex = cursor getColumnIndex "source_id"

    val list = (0 to cursor.getCount - 1).map { i =>
      cursor.moveToPosition(i)
      cursor.getLong(sourceIdIndex) ->
        cursor.getInt(countIndex)
    }
    val pairs = list.scanLeft((0L, (0, 0))){
      case ((_, (previous, sum)), (sourceId, count)) =>
        (sourceId, (count, previous + sum))
    } map {
      case (sourceId, (count, position)) =>
        sourceId -> position
    }
    pairs.toMap
  }
}
