package x7c1.linen.repository.entry.unread

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.EntryRecord
import x7c1.linen.repository.source.unread.UnreadSource
import x7c1.linen.repository.unread.{EntryKind, SourceKind, UnreadRowKind}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.sequence.Sequence

trait EntryAccessor[+A <: UnreadEntry] extends Sequence[EntryRowContent[A]] {

  def latestEntriesTo(position: Int): Seq[A]

  def lastEntriesTo(position: Int): Seq[A]

  def firstEntryPositionOf(sourceId: Long): Option[Int]

  def findKindAt(position: Int): Option[UnreadRowKind]
}

trait ClosableEntryAccessor[+A <: UnreadEntry] extends EntryAccessor[A]{
  def close(): Unit
}

class EntryAccessorImpl[A <: UnreadEntry](
  entrySequence: EntrySequence[A],
  positions: EntrySourcePositions,
  cursor: Cursor ) extends ClosableEntryAccessor[A] {

  private lazy val sequence = positions.toHeadlines mergeWith entrySequence

  override def findAt(position: Int) = {
    sequence.findAt(position) map {
      case Left(source) => source
      case Right(entry) => EntryContent(entry)
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

  override def lastEntriesTo(position: Int) = {
    val targets = positions.lastEntryPositions.view takeWhile {_ < position}
    targets :+ position flatMap findAt collect {
      case EntryContent(entry) => entry
    }
  }
  override def latestEntriesTo(position: Int): Seq[A] = {
    val targets = positions.latestEntryPositions.view takeWhile {_ <= position}
    targets flatMap findAt collect {
      case EntryContent(entry) => entry
    }
  }
}

trait EntryFactory[A <: UnreadEntry]{
  def createEntry(): A
}

class EntryOutlineFactory(rawCursor: Cursor) extends EntryFactory[UnreadOutline] {
  private lazy val cursor = TypedCursor[UnreadEntryRecord](rawCursor)

  override def createEntry(): UnreadOutline = {
    UnreadOutline(
      entryId = cursor.entry_id,
      sourceId = cursor.source_id,
      url = cursor.url,
      accountId = cursor.account_id,
      shortTitle = cursor.title,
      shortContent = cursor.content,
      createdAt = cursor.created_at.typed
    )
  }
}

class EntryDetailFactory(rawCursor: Cursor) extends EntryFactory[UnreadDetail] {
  private lazy val cursor = TypedCursor[UnreadEntryRecord](rawCursor)

  override def createEntry(): UnreadDetail = {
    UnreadDetail(
      entryId = cursor.entry_id,
      sourceId = cursor.source_id,
      accountId = cursor.account_id,
      url = cursor.url,
      fullTitle = cursor.title,
      fullContent = cursor.content,
      createdAt = cursor.created_at.typed
    )
  }
}

trait UnreadEntryRecord extends EntryRecord {
  def account_id: Long
}

object EntryAccessor {

  def forEntryOutline(
    db: SQLiteDatabase, sources: Seq[UnreadSource],
    positions: EntrySourcePositions): ClosableEntryAccessor[UnreadOutline] = {

    val cursor = createOutlineCursor(db, sources)
    val factory = new EntryOutlineFactory(cursor)
    val sequence = new EntrySequence(factory, cursor)
    new EntryAccessorImpl(sequence, positions, cursor)
  }
  def forEntryDetail(
    db: SQLiteDatabase, sources: Seq[UnreadSource],
    positions: EntrySourcePositions): ClosableEntryAccessor[UnreadDetail] = {

    val cursor = createDetailCursor(db, sources)
    val factory = new EntryDetailFactory(cursor)
    val sequence = new EntrySequence(factory, cursor)
    new EntryAccessorImpl(sequence, positions, cursor)
  }

  def createOutlineCursor(db: SQLiteDatabase, sources: Seq[UnreadSource]) = {
    createCursor(db, sources, "substr(content, 1, 75) AS content")
  }
  def createDetailCursor(db: SQLiteDatabase, sources: Seq[UnreadSource]) = {
    createCursor(db, sources, "content")
  }
  def createCursor(db: SQLiteDatabase, sources: Seq[UnreadSource], content: String) = {
    val sql =
      s"""SELECT
        |  entry_id,
        |  source_id,
        |  title,
        |  url,
        |  author,
        |  $content,
        |  created_at,
        |  ? AS account_id
        |FROM entries
        |WHERE ${QueryParts.where}
        |ORDER BY created_at DESC
        |LIMIT ${QueryParts.limit}""".stripMargin

    val union = sources.
      map(_ => s"SELECT * FROM ($sql) AS tmp").
      mkString(" UNION ALL ")

    db.rawQuery(union, QueryParts.toArgs(sources))
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
