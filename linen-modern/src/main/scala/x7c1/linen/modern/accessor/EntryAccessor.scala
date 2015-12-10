package x7c1.linen.modern.accessor

import android.content.Context
import android.database.Cursor
import x7c1.linen.modern.struct.{Date, Entry}
import x7c1.wheat.macros.logger.Log

trait EntryAccessor {

  def findAt(position: Int): Option[Entry]

  def length: Int

  def firstEntryPositionOf(sourceId: Long): Option[Int]
}

class EntryBuffer(cursor: Cursor, positionMap: Map[Long, Int]) extends EntryAccessor {

  private lazy val entryIdIndex = cursor getColumnIndex "entry_id"
  private lazy val sourceIdIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"
  private lazy val contentIndex = cursor getColumnIndex "content"
  private lazy val createdAtIndex = cursor getColumnIndex "created_at"

  override def findAt(position: Int) = try {
    cursor moveToPosition position
    Some apply Entry(
      entryId = cursor getInt entryIdIndex,
      sourceId = cursor getInt sourceIdIndex,
      url = "dummy",
      title = cursor getString titleIndex,
      content = cursor getString contentIndex,
      createdAt = Date.dummy()
    )
  } catch {
    case e: Exception =>

      /*
        In rare case, somehow CursorWindow throws an IllegalStateException which says:
          "Make sure the Cursor is initialized correctly before accessing data from it."
       */
      Log error e.getStackTrace.take(20).mkString("\n")
      None
  }

  override def length = {
    cursor.getCount
  }
  override def firstEntryPositionOf(sourceId: Long): Option[Int] = {
    positionMap.get(sourceId)
  }
}

object EntryBuffer {

  def createOutline(context: Context): EntryBuffer = {
    val content = createSql4("substr(entries.content, 1, 100)")
    val cursor = createCursor(context, content)
    val map = createSourcePositionMap(context, content)
    new EntryBuffer(cursor, map)
  }

  def createFullContent(context: Context): EntryBuffer = {
    val content = createSql4("entries.content")
    val cursor = createCursor(context, content)
    val map = createSourcePositionMap(context, content)
    new EntryBuffer(cursor, map)
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

  def createCursor(context: Context, sql4: String) = {
    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    db.rawQuery(sql4, Array())
  }

  def createCounterCursor(context: Context, sql4: String) = {
    val sql5 =
      s"""SELECT COUNT(entry_id) as count, source_id, rating
         | FROM($sql4)
         | GROUP BY source_id, rating
         | ORDER BY rating DESC, source_id DESC
       """.stripMargin

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    db.rawQuery(sql5, Array())
  }

  def createSourcePositionMap(context: Context, sql4: String): Map[Long, Int] = {
    val cursor = createCounterCursor(context, sql4)
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

object DummyCreator {

  def sourceLength = 300

  def entryLength = sourceLength * entriesPerSource

  def entriesPerSource = 10

  def createEntriesOf(sourceId: Long): Seq[Entry] =
    (1 to entriesPerSource) map { n =>
      createEntry(sourceId, n)
    }

  def createEntryAt(position: Int): Entry = {
    val sourceId = (position / entriesPerSource) + 1
    val n = position % entriesPerSource + 1
    createEntry(sourceId, n)
  }

  def createEntry(sourceId: Long, n: Int) = {
    Entry(
      sourceId = sourceId,
      entryId = sourceId * 1000 + n,
      url = s"http://example.com/source-$sourceId/entry-$n",
      title = s"$sourceId-$n entry " * 3,
      content = s"$sourceId-$n foo bar " * 200,
      createdAt = Date.dummy()
    )
  }

  def entryIndexOf(entryId: Long): Int = {
    val mod = entryId % 1000 - 1
    val n = entryId / 1000 - 1
    (n * entriesPerSource + mod).toInt
  }

  def firstEntryIdOf(sourceId: Long): Option[Long] = {
    Some(sourceId * 1000 + 1)
  }
}
