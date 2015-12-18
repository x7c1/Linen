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
  cursor: Cursor ) extends EntryAccessor[A] {

  override def findAt(position: Int) = synchronized {
    if (cursor moveToPosition position){
      Some apply factory.createEntry()
    } else None
  }
  override def length = {
    cursor.getCount
  }
  override def firstEntryPositionOf(sourceId: Long): Option[Int] = {
    (0 to length - 1).view.map(findAt).zipWithIndex.collectFirst {
      case (Some(entry), n) if entry.sourceId == sourceId => n
    }
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
    context: Context, accessor: SourceAccessor): EntryAccessor[EntryOutline] = {

    val sources = (0 to accessor.length - 1).map(accessor.findAt)
    val sourceIds = sources.flatMap(_.map(_.id))

    val cursor = createOutlineCursor(context, sourceIds)
    val factory = new EntryOutlineFactory(cursor)
    new EntryAccessorImpl(factory, cursor)
  }
  def forEntryDetail(
    context: Context, accessor: SourceAccessor): EntryAccessor[EntryDetail] = {

    val sources = (0 to accessor.length - 1).map(accessor.findAt)
    val sourceIds = sources.flatMap(_.map(_.id))

    val cursor = createDetailCursor(context, sourceIds)
    val factory = new EntryDetailFactory(cursor)
    new EntryAccessorImpl(factory, cursor)
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
}
