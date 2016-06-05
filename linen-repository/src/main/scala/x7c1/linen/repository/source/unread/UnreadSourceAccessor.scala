package x7c1.linen.repository.source.unread

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.{HasAccountId, HasChannelId}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.sequence.Sequence

import scala.util.Try

trait UnreadSourceAccessor extends Sequence[SourceRowContent] {

  def sources: Seq[UnreadSource] = {
    (0 until length).view flatMap findAt collect {
      case x: UnreadSource => x
    }
  }
  def positionOf(sourceId: Long): Option[Int]
}

trait ClosableSourceAccessor extends UnreadSourceAccessor {
  def close(): Unit
}

private class UnreadSourceAccessorImpl(
  rawCursor: Cursor,
  positionMap: Map[Int, Int],
  sourceIdMap: Map[Long, Int]) extends ClosableSourceAccessor {

  private lazy val cursor = TypedCursor[UnreadSourceColumn](rawCursor)

  override def findAt(position: Int) = synchronized {
    if (position > length - 1) {
      None
    } else (cursor moveToFind positionMap(position)){
      UnreadSource(
        id = cursor.source_id,
        url = "dummy",
        title = cursor.title,
        description = cursor.description,
        rating = cursor.rating,
        accountId = cursor.account_id,
        latestEntryId = cursor.latest_entry_id,
        latestEntryCreatedAt = cursor.latest_entry_created_at,
        startEntryId = cursor.start_entry_id,
        startEntryCreatedAt = cursor.start_entry_created_at
      )
    }
  }
  override def length = {
    rawCursor.getCount
  }
  override def positionOf(sourceId: Long): Option[Int] = {
    sourceIdMap.get(sourceId)
  }
  override def close(): Unit = rawCursor.close()
}

object UnreadSourceAccessor {
  def create[A: HasAccountId, B: HasChannelId](
    db: SQLiteDatabase,
    accountId: A, channelId: B): Try[ClosableSourceAccessor] = {

    Try {
      val cursor = UnreadSourceAccessor.createCursor(db, channelId, accountId)
      val (positionMap, sourceIdMap) = createMaps(cursor)
      new UnreadSourceAccessorImpl(cursor, positionMap, sourceIdMap)
    }
  }
  private def createMaps(rawCursor: Cursor) = {
    val cursor = TypedCursor[UnreadSourceColumn](rawCursor)
    val sorted = 0 until rawCursor.getCount flatMap { n =>
      cursor.moveToFind(n)((n, cursor.rating, cursor.source_id))
    } sortWith {
      case ((_, rating1, _), (_, rating2, _)) =>
        rating1 >= rating2
    }
    val indexed = sorted.zipWithIndex
    val pairs1 = indexed map { case ((n, _, _), index) => index -> n }
    val pairs2 = indexed map { case ((_, _, sourceId), index) => sourceId -> index }
    pairs1.toMap -> pairs2.toMap
  }
  def createCursor[A: HasAccountId, B: HasChannelId]
    (db: SQLiteDatabase, channelId: B, accountId: A) = {
    val query = createQuery(channelId, accountId)
    db.rawQuery(query.sql, query.selectionArgs)
  }
  def createQuery[A: HasAccountId, B: HasChannelId](channel: B, account: A) = {
    val accountId = implicitly[HasAccountId[A]] toId account
    val channelId = implicitly[HasChannelId[B]] toId channel
    val sql = UnreadSourceAccessorQueries.sql5
    new Query(sql,
      Array(
        accountId.toString, accountId.toString,
        channelId.toString, accountId.toString))
  }
}
