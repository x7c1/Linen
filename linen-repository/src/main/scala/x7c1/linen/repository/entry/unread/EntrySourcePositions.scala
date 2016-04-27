package x7c1.linen.repository.entry.unread
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.Query

import x7c1.wheat.modern.sequence.{Sequence, SequenceHeadlines}

object EntrySourcePositions {
  def createPositionQuery(accountId: Long, sourceIds: Seq[Long]): Query = {
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
  def createPositionCursor(db: SQLiteDatabase, accountId: Long, sourceIds: Seq[Long]) = {
    val query = createPositionQuery(accountId, sourceIds)
    db.rawQuery(query.sql, query.selectionArgs)
  }
  def createPositionMap(db: SQLiteDatabase, accountId: Long, sourceIds: Seq[Long]): EntrySourcePositions = {
    val cursor = createPositionCursor(db, accountId, sourceIds)
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
    new EntrySourcePositions(cursor, pairs.toMap)
  }
}
class EntrySourcePositions(cursor: Cursor, countMap: Map[Long, Int]) extends Sequence[SourceHeadlineContent] {

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
