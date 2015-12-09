package x7c1.linen.modern.accessor

import android.content.Context
import android.database.Cursor
import x7c1.linen.modern.struct.{Date, Entry}

trait EntryAccessor {

  def get(position: Int): Entry

  def length: Int

  def firstEntryIdOf(sourceId: Long): Option[Long]

  def indexOf(entryId: Long): Int
}

class EntryBuffer(cursor: Cursor) extends EntryAccessor {

  private lazy val entryIdIndex = cursor getColumnIndex "entry_id"
  private lazy val sourceIdIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"
  private lazy val contentIndex = cursor getColumnIndex "content"
  private lazy val createdAtIndex = cursor getColumnIndex "created_at"

  override def get(position: Int): Entry = {
    cursor moveToPosition position
    Entry(
      entryId = cursor getInt entryIdIndex,
      sourceId = cursor getInt sourceIdIndex,
      url = "dummy",
      title = cursor getString titleIndex,
      content = cursor getString contentIndex,
      createdAt = Date.dummy()
    )
  }
  override def length = {
    cursor.getCount
  }
  override def indexOf(entryId: Long): Int = {
    (0 to length - 1) find { i =>
      get(i).entryId == entryId
    } getOrElse {
      throw new IllegalArgumentException(s"entry($entryId) not found")
    }
  }
  override def firstEntryIdOf(sourceId: Long): Option[Long] = {
    (0 to length - 1).view map get collectFirst {
      case entry if entry.sourceId == sourceId => entry.entryId
    }
  }

}

object EntryBuffer {
  def create(context: Context): EntryBuffer = {
    val cursor = createCursor(context)
    new EntryBuffer(cursor)
  }
  def createCursor(context: Context) = {
    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    val sql1 =
      """SELECT * FROM list_source_map
        | INNER JOIN sources ON list_source_map.source_id = sources._id
        | ORDER BY sources._id DESC""".stripMargin

    val sql2 =
      """SELECT source_id FROM entries
        | WHERE read_state = 0
        | GROUP BY source_id """.stripMargin

    val sql3 =
      s"""SELECT
        |   s1._id as source_id,
        |   s1.title as title,
        |   s1.description as description
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
        | FROM entries""".stripMargin

    val sql4 =
      s"""SELECT
        |   entries._id as entry_id,
        |   s3.source_id as source_id,
        |   entries.title as title,
        |   entries.content as content,
        |   entries.created_at as created_at
        | FROM ($sql3) as s3
        | INNER JOIN ($entries) as entries
        | ON s3.source_id = entries.source_id""".stripMargin

    db.rawQuery(sql4, Array())
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
