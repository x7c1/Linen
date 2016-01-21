package x7c1.linen.modern.accessor

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.{Date, Entry, EntryDetail, EntryOutline}
import x7c1.wheat.macros.database.TypedCursor

import scala.annotation.tailrec

trait EntryAccessor[+A <: Entry]{

  def findAt(position: Int): Option[A]

  def length: Int

  def firstEntryPositionOf(sourceId: Long): Option[Int]
}

class EntryAccessorBinder[A <: Entry](
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

class EntryOutlineFactory(rawCursor: Cursor) extends EntryFactory[EntryOutline] {
  private lazy val cursor = TypedCursor[EntryRecordColumn](rawCursor)

  override def createEntry(): EntryOutline = {
    EntryOutline(
      entryId = cursor.entry_id,
      sourceId = cursor.source_id,
      url = "dummy",
      shortTitle = cursor.title,
      shortContent = cursor.content,
      createdAt = cursor.created_at.typed
    )
  }
}

class EntryDetailFactory(rawCursor: Cursor) extends EntryFactory[EntryDetail] {
  private lazy val cursor = TypedCursor[EntryRecordColumn](rawCursor)

  override def createEntry(): EntryDetail = {
    EntryDetail(
      entryId = cursor.entry_id,
      sourceId = cursor.source_id,
      url = "dummy",
      fullTitle = cursor.title,
      fullContent = cursor.content,
      createdAt = cursor.created_at.typed
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

trait EntryRecordColumn extends TypedCursor {
  def entry_id: Long
  def source_id: Long
  def title: String
  def content: String
  def url: String
  def created_at: Int --> Date
}

case class EntryParts(
  sourceId: Long,
  title: String,
  content: String,
  url: String,
  createdAt: Date
)
object EntryParts {
  implicit object insertable extends Insertable[EntryParts] {
    override def tableName: String = "entries"
    override def toContentValues(target: EntryParts): ContentValues = {
      val column = TypedCursor.expose[EntryRecordColumn]
      TypedCursor toContentValues (
        column.source_id -> target.sourceId,
        column.title -> target.title,
        column.content -> target.content,
        column.url -> target.url,
        column.created_at -> target.createdAt
      )
    }
  }
}
