package x7c1.linen.modern.accessor.unread

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView.ViewHolder
import x7c1.linen.database.Query
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.macros.logger.Log

import scala.util.Try

trait UnreadSourceAccessor extends UnreadItemAccessor {

  def sourceIds: Seq[Long] = {
    (0 to length - 1).view map findAt flatMap {
      _ flatMap {
        case UnreadSourceRow(x: UnreadSource) => Some(x.id)
        case _ => None
      }
    }
  }
  def findAt(position: Int): Option[UnreadSourceRow]

  def positionOf(sourceId: Long): Option[Int]

  def bindViewHolder[B <: ViewHolder]
    (holder: B, position: Int)
    (block: PartialFunction[(B, SourceRowContent), Unit]) = {

    findAt(position) -> holder match {
      case (Some(UnreadSourceRow(item)), _) if block isDefinedAt (holder, item) =>
        block(holder, item)
      case (item, _) =>
        Log error s"unknown item:$item, holder:$holder"
    }
  }
}

trait ClosableSourceAccessor extends UnreadSourceAccessor {
  def close(): Unit
}

case class UnreadSourceRow(content: SourceRowContent){
  def source: Option[UnreadSource] = content match {
    case x: UnreadSource => Some(x)
    case _ => None
  }
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
      UnreadSourceRow apply UnreadSource(
        id = cursor.source_id,
        url = "dummy",
        title = cursor.title,
        description = cursor.description,
        rating = cursor.rating,
        latestEntryId = cursor.latest_entry_id,
        startEntryId = cursor.start_entry_id
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
  def create(
    db: SQLiteDatabase,
    accountId: Long, channelId: Long): Try[ClosableSourceAccessor] = {

    Try {
      val cursor = UnreadSourceAccessor.createCursor(db, channelId, accountId)
      val (positionMap, sourceIdMap) = createMaps(cursor)
      new UnreadSourceAccessorImpl(cursor, positionMap, sourceIdMap)
    }
  }
  private def createMaps(rawCursor: Cursor) = {
    val cursor = TypedCursor[UnreadSourceColumn](rawCursor)
    val sorted = (0 to rawCursor.getCount - 1) flatMap { n =>
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
  def createCursor(db: SQLiteDatabase, channelId: Long, accountId: Long) = {
    val query = createQuery(channelId, accountId)
    db.rawQuery(query.sql, query.selectionArgs)
  }
  def createQuery(channelId: Long, accountId: Long) = {
    val sql = UnreadSourceAccessorQueries.sql5
    new Query(sql,
      Array(accountId.toString, channelId.toString, accountId.toString))
  }
}
