package x7c1.linen.modern.accessor

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.{Date, Entry, EntryDetail, EntryOutline}

import scala.annotation.tailrec

trait EntryAccessor[+A <: Entry]{

  def findAt(position: Int): Option[A]

  def length: Int

  def firstEntryPositionOf(sourceId: Long): Option[Int]
}

class EntryAccessorHolder[A <: Entry](
  accessors: Seq[EntryAccessor[A]]) extends EntryAccessor[A]{

  override def findAt(position: Int): Option[A] = {
    @tailrec
    def loop(accessors: Seq[EntryAccessor[A]], prev: Int): Option[(EntryAccessor[A], Int)] =
      accessors match {
        case x +: xs => x.length + prev match {
          case sum if sum > position => Some(x -> prev)
          case sum => loop(xs, sum)
        }
        case Seq() => None
      }

    loop(accessors, 0) flatMap { case (accessor, prev) =>
      accessor.findAt(position - prev)
    }
  }

  override def length: Int = {
    accessors.foldLeft(0){ _ + _.length }
  }

  override def firstEntryPositionOf(sourceId: Long): Option[Int] = {
    @tailrec
    def loop(accessors: Seq[EntryAccessor[A]], prev: Int): Option[Int] =
      accessors match {
        case x +: xs => x.firstEntryPositionOf(sourceId) match {
          case Some(s) => Some(prev + s)
          case None => loop(xs, x.length + prev)
        }
        case Seq() => None
      }

    loop(accessors, 0)
  }
}

class EntryAccessorImpl[A <: Entry](
  factory: EntryFactory[A],
  cursor: Cursor,
  positions: Map[Long, Int]) extends EntryAccessor[A] {

  override def findAt(position: Int) = synchronized {
    if (cursor moveToPosition position){
      Some apply factory.createEntry()
    } else None
  }
  override lazy val length = {
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
    db: SQLiteDatabase, sourceIds: Seq[Long],
    positionMap: Map[Long, Int]): EntryAccessor[EntryOutline] = {

    val cursor = createOutlineCursor(db, sourceIds)
    val factory = new EntryOutlineFactory(cursor)
    new EntryAccessorImpl(factory, cursor, positionMap)
  }
  def forEntryDetail(
    db: SQLiteDatabase, sourceIds: Seq[Long],
    positionMap: Map[Long, Int]): EntryAccessor[EntryDetail] = {

    val cursor = createDetailCursor(db, sourceIds)
    val factory = new EntryDetailFactory(cursor)
    new EntryAccessorImpl(factory, cursor, positionMap)
  }

  def createOutlineCursor(db: SQLiteDatabase, sourceIds: Seq[Long]) = {
    createCursor(db, sourceIds, "substr(content, 1, 75) AS content")
  }
  def createDetailCursor(db: SQLiteDatabase, sourceIds: Seq[Long]) = {
    createCursor(db, sourceIds, "content")
  }
  def createCursor(db: SQLiteDatabase, sourceIds: Seq[Long], content: String) = {
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

    db.rawQuery(union, sourceIds.map(_.toString).toArray)
  }
  def createPositionCursor(db: SQLiteDatabase, sourceIds: Seq[Long]) = {
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

    db.rawQuery(union, sourceIds.map(_.toString).toArray)
  }
  def createPositionMap(db: SQLiteDatabase, sourceIds: Seq[Long]): Map[Long, Int] = {
    val cursor = createPositionCursor(db, sourceIds)
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
