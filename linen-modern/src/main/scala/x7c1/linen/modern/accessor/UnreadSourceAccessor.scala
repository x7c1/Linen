package x7c1.linen.modern.accessor

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.accessor.unread.UnreadSourceAccessorQueries
import x7c1.linen.modern.struct.{Date, UnreadSource}
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}

import scala.util.Try

trait UnreadSourceAccessor {

  def sourceIds: Seq[Long] = {
    (0 to length - 1).map(findAt).flatMap(_.map(_.id))
  }
  def findAt(position: Int): Option[UnreadSource]

  def length: Int

  def positionOf(sourceId: Long): Option[Int]
}

private class UnreadSourceAccessorImpl(
  rawCursor: Cursor,
  positionMap: Map[Int, Int],
  sourceIdMap: Map[Long, Int]) extends UnreadSourceAccessor {

  private lazy val cursor = TypedCursor[UnreadSourceColumn](rawCursor)

  override def findAt(position: Int) = synchronized {
    val n = positionMap(position)
    cursor.moveToFind(n){
      UnreadSource(
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
}

object UnreadSourceAccessor {
  def create(
    db: SQLiteDatabase,
    accountId: Long, channelId: Long): Try[UnreadSourceAccessor] = {

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

trait UnreadSourceColumn extends TypedFields {
  def source_id: Long
  def title: String
  def description: String
  def rating: Int
  def start_entry_id: Option[Long]
  def latest_entry_id: Long
}

trait SourceRecordColumn extends TypedFields {
  def _id: Long
  def title: String
  def description: String
  def url: String
  def created_at: Int --> Date
}
object SourceRecordColumn {
  implicit object selectable extends SingleSelectable[SourceRecordColumn, Long]{
    override def tableName: String = "sources"
    override def where(id: Long): Seq[(String, String)] = Seq(
      "_id" -> id.toString
    )
    override def fromCursor(rawCursor: Cursor): Option[SourceRecordColumn] = {
      TypedCursor[SourceRecordColumn](rawCursor) freezeAt 0
    }
  }
}

case class SourceParts(
  title: String,
  url: String,
  description: String,
  createdAt: Date
)
object SourceParts {
  implicit object insertable extends Insertable[SourceParts] {
    override def tableName: String = "sources"
    override def toContentValues(target: SourceParts): ContentValues = {
      val column = TypedFields.expose[SourceRecordColumn]
      TypedFields toContentValues (
        column.title -> target.title,
        column.url -> target.url,
        column.description -> target.description,
        column.created_at -> target.createdAt
      )
    }
  }
}

case class SourceStatusParts(
  sourceId: Long,
  accountId: Long,
  createdAt: Date
)
object SourceStatusParts {
  implicit object insertable extends Insertable[SourceStatusParts] {
    override def tableName: String = "source_statuses"
    override def toContentValues(target: SourceStatusParts): ContentValues = {
      val values = new ContentValues()
      values.put("source_id", target.sourceId: java.lang.Long)
      values.put("account_id", target.accountId: java.lang.Long)
      values.put("created_at", target.createdAt.timestamp: java.lang.Integer)
      values
    }
  }
}
case class SourceStatusAsStarted(
  startEntryId: Long,
  sourceId: Long,
  accountId: Long
)
object SourceStatusAsStarted {
  implicit object updatable extends Updatable[SourceStatusAsStarted] {
    override def tableName: String = "source_statuses"
    override def toContentValues(target: SourceStatusAsStarted): ContentValues = {
      val values = new ContentValues()
      values.put("start_entry_id", target.startEntryId: java.lang.Long)
      values
    }
    override def where(target: SourceStatusAsStarted) = Seq(
      "source_id" -> target.sourceId.toString,
      "account_id" -> target.accountId.toString
    )
  }
}

case class SourceRatingParts(
  sourceId: Long,
  accountId: Long,
  rating: Int,
  createdAt: Date
)
object SourceRatingParts {
  implicit object insertable extends Insertable[SourceRatingParts] {
    override def tableName: String = "source_ratings"
    override def toContentValues(target: SourceRatingParts): ContentValues = {
      val values = new ContentValues()
      values.put("source_id", target.sourceId: java.lang.Long)
      values.put("account_id", target.accountId: java.lang.Long)
      values.put("rating", target.rating: java.lang.Integer)
      values.put("created_at", target.createdAt.timestamp: java.lang.Integer)
      values
    }
  }
}

trait ChannelSourceMapColumn extends TypedFields {
  def source_id: Long
  def channel_id: Long
  def created_at: Int --> Date
}
case class ChannelSourceMapParts(
  channelId: Long,
  sourceId: Long,
  createdAt: Date
)
object ChannelSourceMapParts {
  implicit object insertable extends Insertable[ChannelSourceMapParts] {
    override def tableName: String = "channel_source_map"
    override def toContentValues(target: ChannelSourceMapParts): ContentValues = {
      val column = TypedFields.expose[ChannelSourceMapColumn]
      TypedFields toContentValues (
        column.source_id -> target.sourceId,
        column.channel_id -> target.channelId,
        column.created_at -> target.createdAt
      )
    }
  }
}
