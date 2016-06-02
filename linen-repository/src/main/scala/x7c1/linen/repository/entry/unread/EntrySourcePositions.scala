package x7c1.linen.repository.entry.unread
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.source.unread.UnreadSource
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.sequence.{Sequence, SequenceHeadlines}

object EntrySourcePositions {
  def createQuery(sources: Seq[UnreadSource]): Query = {

    val count =
      s"""SELECT
         |  entry_id,
         |  source_id,
         |  ? AS account_id
         |FROM entries
         |WHERE ${QueryParts.where}
         |LIMIT ${QueryParts.limit}""".stripMargin

    val sql =
      s"""SELECT
         | source_id,
         | title,
         | COUNT(entry_id) AS count
         |FROM ($count) AS c1
         |INNER JOIN sources as s1 ON s1._id = c1.source_id""".stripMargin

    val union = sources.
      map(_ => s"SELECT * FROM ($sql) AS tmp").
      mkString(" UNION ALL ")

    new Query(union, QueryParts.toArgs(sources))
  }
}

class EntrySourcePositions(
  cursor: Cursor,
  countMap: Map[Long, Int]) extends Sequence[SourceHeadlineContent] {

  private lazy val countIndex = cursor getColumnIndex "count"
  private lazy val sourceIdIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"

  private lazy val pairs: Seq[(Int, Boolean)] = {
    val counts = 0 until cursor.getCount map { i =>
      cursor moveToPosition i
      cursor.getInt(countIndex)
    }
    counts.scanLeft(0 -> true){
      case ((position, bool), count) =>
        (position + count + 1) -> true
    }
  }
  private lazy val positionMap: Map[Int, Boolean] = {
    pairs.toMap
  }
  lazy val lastEntryPositions: Seq[Int] = {
    pairs.view map { case (position, _) => position - 1 } dropWhile { _ < 0 }
  }
  lazy val latestEntryPositions: Seq[Int] = {
    pairs.view map { case (position, _) => position + 1 } dropWhile { _ < 0 }
  }
  def isSource(position: Int): Boolean = {
    positionMap.getOrElse(position, false)
  }

  def toHeadlines: SequenceHeadlines[SourceHeadlineContent] = {
    val list = 0 until cursor.getCount map { i =>
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

class EntrySourcePositionsFactory(db: SQLiteDatabase){

  def createCursor(sources: Seq[UnreadSource]): Cursor = {
    val query = EntrySourcePositions.createQuery(sources)
    db.rawQuery(query.sql, query.selectionArgs)
  }
  def create(sources: Seq[UnreadSource]): EntrySourcePositions = {
    val cursor = createCursor(sources)
    val countIndex = cursor getColumnIndex "count"
    val sourceIdIndex = cursor getColumnIndex "source_id"
    val list = (0 until cursor.getCount).view map { i =>
      cursor moveToPosition i
      cursor.getLong(sourceIdIndex) -> cursor.getInt(countIndex)
    }
    val pairs = list.scanLeft(0L -> 0 -> 0){
      case (((_, previous), sum), (sourceId, count)) =>
        sourceId -> (count + 1) -> (previous + sum)
    } map {
      case ((sourceId, count), position) =>
        sourceId -> (position + 1)
    }
    new EntrySourcePositions(cursor, pairs.toMap)
  }
}
