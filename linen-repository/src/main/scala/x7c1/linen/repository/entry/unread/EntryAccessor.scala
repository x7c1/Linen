package x7c1.linen.repository.entry.unread

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView.ViewHolder
import x7c1.linen.database.struct.EntryRecord
import x7c1.linen.repository.unread.{EntryKind, SourceKind, UnreadRowKind}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.sequence.{Sequence, SequenceHeadlines}

import scala.annotation.tailrec

trait EntryAccessor[+A <: UnreadEntry] extends Sequence[UnreadEntryRow[A]] {

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

trait ClosableEntryAccessor[+A <: UnreadEntry] extends EntryAccessor[A]{
  def close(): Unit
}

case class UnreadEntryRow[+A <: UnreadEntry](content: EntryRowContent[A]){
  def sourceId: Option[Long] = content match {
    case SourceHeadlineContent(sourceId, title) => Some(sourceId)
    case EntryContent(entry) => Some(entry.sourceId)
    case FooterContent() => None
  }
}

class EntryAccessorBinder[A <: UnreadEntry](
  accessors: Seq[ClosableEntryAccessor[A]]) extends ClosableEntryAccessor[A]{

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

  override def close(): Unit = synchronized {
    accessors foreach (_.close())
  }
}

class EntryAccessorImpl[A <: UnreadEntry](
  entrySequence: EntrySequence[A],
  positions: EntrySourcePositions,
  cursor: Cursor ) extends ClosableEntryAccessor[A] {

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
  override def close(): Unit = cursor.close()
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
    positions: EntrySourcePositions): ClosableEntryAccessor[UnreadOutline] = {

    val cursor = createOutlineCursor(db, sourceIds)
    val factory = new EntryOutlineFactory(cursor)
    val sequence = new EntrySequence(factory, cursor)
    new EntryAccessorImpl(sequence, positions, cursor)
  }
  def forEntryDetail(
    db: SQLiteDatabase, sourceIds: Seq[Long],
    positions: EntrySourcePositions): ClosableEntryAccessor[UnreadDetail] = {

    val cursor = createDetailCursor(db, sourceIds)
    val factory = new EntryDetailFactory(cursor)
    val sequence = new EntrySequence(factory, cursor)
    new EntryAccessorImpl(sequence, positions, cursor)
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
        |  author,
        |  $content,
        |  created_at
        |FROM entries
        |WHERE source_id = ?
        |ORDER BY created_at DESC LIMIT 20""".stripMargin

    val union = sourceIds.
      map(_ => s"SELECT * FROM ($sql) AS tmp").
      mkString(" UNION ALL ")

    db.rawQuery(union, sourceIds.map(_.toString).toArray)
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


