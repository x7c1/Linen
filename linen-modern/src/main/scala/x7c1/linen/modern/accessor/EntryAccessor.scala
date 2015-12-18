package x7c1.linen.modern.accessor

import android.content.Context
import android.database.Cursor
import x7c1.linen.modern.struct.{Date, Entry, EntryDetail, EntryOutline}

trait EntryAccessor[+A <: Entry]{

  def findAt(position: Int): Option[A]

  def length: Int

  def firstEntryPositionOf(sourceId: Long): Option[Int]
}

private class EntryAccessorImpl[A <: Entry](
  factory: EntryFactory[A],
  cursor: Cursor,
  positions: Map[Long, Int]) extends EntryAccessor[A] {

  override def findAt(position: Int) = synchronized {
    if (cursor moveToPosition position){
      Some apply factory.createEntry()
    } else None
  }
  override def length = {
    cursor.getCount
  }
  override def firstEntryPositionOf(sourceId: Long): Option[Int] = {
    positions.get(sourceId)
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

  def forEntryOutline(
    context: Context, sourceIds: Seq[Long],
    positionMap: Map[Long, Int]): EntryAccessor[EntryOutline] = {

    val cursor = createOutlineCursor(context, sourceIds)
    val factory = new EntryOutlineFactory(cursor)
    new EntryAccessorImpl(factory, cursor, positionMap)
  }
  def forEntryDetail(
    context: Context, sourceIds: Seq[Long],
    positionMap: Map[Long, Int]): EntryAccessor[EntryDetail] = {

    val cursor = createDetailCursor(context, sourceIds)
    val factory = new EntryDetailFactory(cursor)
    new EntryAccessorImpl(factory, cursor, positionMap)
  }

  def createOutlineCursor(context: Context, sourceIds: Seq[Long]) = {
    createCursor(context, sourceIds, "substr(content, 1, 75) AS content")
  }
  def createDetailCursor(context: Context, sourceIds: Seq[Long]) = {
    createCursor(context, sourceIds, "content")
  }
  def createCursor(context: Context, sourceIds: Seq[Long], content: String) = {
    val sql =
      s"""SELECT
        |  _id AS entry_id,
        |  source_id,
        |  title,
        |  $content,
        |  created_at
        |FROM entries
        |WHERE source_id = ?
        |ORDER BY entry_id DESC LIMIT 20""".stripMargin

    val union = sourceIds.
      map(_ => s"SELECT * FROM ($sql) AS tmp").
      mkString(" UNION ALL ")

    val db = new LinenOpenHelper(context).getReadableDatabase
    db.rawQuery(union, sourceIds.map(_.toString).toArray)
  }
  def createPositionCursor(context: Context, sourceIds: Seq[Long]) = {
    val count =
      s"""SELECT
         |  _id AS entry_id,
         |  source_id
         |FROM entries
         |WHERE source_id = ?
         |LIMIT 20""".stripMargin

    val sql = s"SELECT source_id, COUNT(entry_id) AS count FROM ($count) AS c1"
    val union = sourceIds.
      map(_ => s"SELECT * FROM ($sql) AS tmp").
      mkString(" UNION ALL ")

    val db = new LinenOpenHelper(context).getReadableDatabase
    db.rawQuery(union, sourceIds.map(_.toString).toArray)
  }
  def createPositionMap(context: Context, sourceIds: Seq[Long]): Map[Long, Int] = {
    val cursor = createPositionCursor(context, sourceIds)
    val countIndex = cursor getColumnIndex "count"
    val sourceIdIndex = cursor getColumnIndex "source_id"
    val list = (0 to cursor.getCount - 1).view map { i =>
      cursor moveToPosition i
      cursor.getLong(sourceIdIndex) -> cursor.getInt(countIndex)
    }
    val pairs = list.scanLeft(0L -> (0, 0)){
      case ((_, (previous, sum)), (sourceId, count)) =>
        sourceId -> (count, previous + sum)
    } map {
      case (sourceId, (count, position)) =>
        sourceId -> position
    }
    cursor.close()
    pairs.toMap
  }
}
