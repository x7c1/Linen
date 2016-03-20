package x7c1.linen.modern.accessor.unread

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView.ViewHolder
import x7c1.linen.modern.accessor.Query
import x7c1.linen.modern.accessor.database.EntryRecord
import x7c1.linen.modern.struct.{UnreadDetail, UnreadEntry, UnreadOutline}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.sequence.{Sequence, SequenceHeadlines}

import scala.annotation.tailrec

trait EntryAccessor[+A <: UnreadEntry] extends UnreadItemAccessor {

  def findAt(position: Int): Option[UnreadEntryRow[A]]

  def firstEntryPositionOf(sourceId: Long): Option[Int]

  def findKindAt(position: Int): Option[UnreadRowKind]

  def bindViewHolder[B <: ViewHolder]
    (holder: B, position: Int)
    (block: PartialFunction[(B, EntryRowContent[A]), Unit]) = {

    findAt(position) -> holder match {
      case (Some(UnreadEntryRow(item)), _) if block isDefinedAt (holder, item) =>
        block(holder, item)
      case (item, _) =>
        Log error s"unknown item:$item, holder:$holder"
    }
  }
  def createPositionMap[B](f: UnreadRowKind => B): Int => B = {
    position => findKindAt(position) match {
      case Some(kind) => f(kind)
      case None =>
        throw new IllegalArgumentException(s"row-kind not defined at $position")
    }
  }
}

case class UnreadEntryRow[+A <: UnreadEntry](content: EntryRowContent[A]){
  def sourceId: Option[Long] = content match {
    case SourceHeadlineContent(sourceId, title) => Some(sourceId)
    case EntryContent(entry) => Some(entry.sourceId)
    case FooterContent() => None
  }
}

class EntryAccessorBinder[A <: UnreadEntry](
  accessors: Seq[EntryAccessor[A]]) extends EntryAccessor[A]{

  override def findAt(position: Int) = {
    findAccessor(accessors, position, 0) flatMap { case (accessor, prev) =>
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

  override def findKindAt(position: Int) = {
    findAccessor(accessors, position, 0) flatMap { case (accessor, prev) =>
      accessor.findKindAt(position - prev)
    }
  }

  @tailrec
  private def findAccessor(
    accessors: Seq[EntryAccessor[A]],
    position: Int,
    prev: Int): Option[(EntryAccessor[A], Int)] = {

    accessors match {
      case x +: xs => x.length + prev match {
        case sum if sum > position => Some(x -> prev)
        case sum => findAccessor(xs, position, sum)
      }
      case Seq() => None
    }
  }

}

class EntryAccessorImpl[A <: UnreadEntry](
  entrySequence: EntrySequence[A],
  positions: SourcePositions) extends EntryAccessor[A] {

  private lazy val sequence = positions.toHeadlines mergeWith entrySequence

  override def findAt(position: Int) = {
    sequence.findAt(position) map {
      case Left(source) => UnreadEntryRow(source)
      case Right(entry) => UnreadEntryRow(EntryContent(entry))
    }
  }
  override lazy val length = {
    sequence.length
  }
  override def firstEntryPositionOf(sourceId: Long): Option[Int] = {
    positions.findEntryPositionOf(sourceId)
  }
  override def findKindAt(position: Int): Option[UnreadRowKind] = {
    val kind = if (positions isSource position) SourceKind else EntryKind
    Some(kind)
  }
}

trait EntryFactory[A <: UnreadEntry]{
  def createEntry(): A
}

class EntryOutlineFactory(rawCursor: Cursor) extends EntryFactory[UnreadOutline] {
  private lazy val cursor = TypedCursor[EntryRecord](rawCursor)

  override def createEntry(): UnreadOutline = {
    UnreadOutline(
      entryId = cursor.entry_id,
      sourceId = cursor.source_id,
      url = cursor.url,
      shortTitle = cursor.title,
      shortContent = cursor.content,
      createdAt = cursor.created_at.typed
    )
  }
}

class EntryDetailFactory(rawCursor: Cursor) extends EntryFactory[UnreadDetail] {
  private lazy val cursor = TypedCursor[EntryRecord](rawCursor)

  override def createEntry(): UnreadDetail = {
    UnreadDetail(
      entryId = cursor.entry_id,
      sourceId = cursor.source_id,
      url = cursor.url,
      fullTitle = cursor.title,
      fullContent = cursor.content,
      createdAt = cursor.created_at.typed
    )
  }
}

object EntryAccessor {

  def forEntryOutline(
    db: SQLiteDatabase, sourceIds: Seq[Long],
    positions: SourcePositions): EntryAccessor[UnreadOutline] = {

    val cursor = createOutlineCursor(db, sourceIds)
    val factory = new EntryOutlineFactory(cursor)
    val sequence = new EntrySequence(factory, cursor)
    new EntryAccessorImpl(sequence, positions)
  }
  def forEntryDetail(
    db: SQLiteDatabase, sourceIds: Seq[Long],
    positions: SourcePositions): EntryAccessor[UnreadDetail] = {

    val cursor = createDetailCursor(db, sourceIds)
    val factory = new EntryDetailFactory(cursor)
    val sequence = new EntrySequence(factory, cursor)
    new EntryAccessorImpl(sequence, positions)
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
        |  url,
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
  def createPositionQuery(sourceIds: Seq[Long]): Query = {
    val count =
      s"""SELECT
         |  _id AS entry_id,
         |  source_id
         |FROM entries
         |WHERE source_id = ?
         |LIMIT 20""".stripMargin

    val sql =
      s"""SELECT
         | source_id,
         | title,
         | COUNT(entry_id) AS count
         |FROM ($count) AS c1
         |INNER JOIN sources as s1 ON s1._id = c1.source_id""".stripMargin

    val union = sourceIds.
      map(_ => s"SELECT * FROM ($sql) AS tmp").
      mkString(" UNION ALL ")

    new Query(union, sourceIds.map(_.toString).toArray)
  }
  def createPositionCursor(db: SQLiteDatabase, sourceIds: Seq[Long]) = {
    val query = createPositionQuery(sourceIds)
    db.rawQuery(query.sql, query.selectionArgs)
  }
  def createPositionMap(db: SQLiteDatabase, sourceIds: Seq[Long]): SourcePositions = {
    val cursor = createPositionCursor(db, sourceIds)
    val countIndex = cursor getColumnIndex "count"
    val sourceIdIndex = cursor getColumnIndex "source_id"
    val list = (0 to cursor.getCount - 1).view map { i =>
      cursor moveToPosition i
      cursor.getLong(sourceIdIndex) -> cursor.getInt(countIndex)
    }
    val pairs = list.scanLeft(0L -> (0, 0)){
      case ((_, (previous, sum)), (sourceId, count)) =>
        sourceId -> (count + 1, previous + sum)
    } map {
      case (sourceId, (count, position)) =>
        sourceId -> (position + 1)
    }
    new SourcePositions(cursor, pairs.toMap)
  }
}

class EntrySequence[A <: UnreadEntry](
  factory: EntryFactory[A],
  cursor: Cursor ) extends Sequence[A]{

  override lazy val length: Int = cursor.getCount

  override def findAt(position: Int): Option[A] = synchronized {
    cursor moveToPosition position match {
      case true => Some(factory.createEntry())
      case false => None
    }
  }
}

class SourcePositions(cursor: Cursor, countMap: Map[Long, Int]) extends Sequence[SourceHeadlineContent] {

  private lazy val countIndex = cursor getColumnIndex "count"
  private lazy val sourceIdIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"

  private lazy val positionMap: Map[Int, Boolean] = {
    val counts = (0 to cursor.getCount - 1) map { i =>
      cursor moveToPosition i
      cursor.getInt(countIndex)
    }
    val pairs = counts.scanLeft(0 -> true){
      case ((position, bool), count) =>
        (position + count + 1) -> true
    }
    pairs.toMap
  }
  def isSource(position: Int): Boolean = {
    positionMap.getOrElse(position, false)
  }

  def toHeadlines: SequenceHeadlines[SourceHeadlineContent] = {
    val list = (0 to cursor.getCount - 1) map { i =>
      cursor moveToPosition i
      cursor.getInt(countIndex)
    }
    SequenceHeadlines.atInterval(this, list)
  }
  def findEntryPositionOf(sourceId: Long): Option[Int] = countMap.get(sourceId)

  override lazy val length: Int = cursor.getCount

  override def findAt(position: Int): Option[SourceHeadlineContent] = {
    cursor moveToPosition position match {
      case true =>
        val id = cursor getLong sourceIdIndex
        Some(new SourceHeadlineContent(
          sourceId = id,
          title = cursor getString titleIndex
        ))
      case false => None
    }
  }
}
