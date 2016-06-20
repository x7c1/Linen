package x7c1.linen.repository.source.setting

import android.database.sqlite.SQLiteDatabase
import android.database.{Cursor, SQLException}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{ChannelSourceMapParts, HasAccountId, HasChannelId, HasSourceId, HasSourceStatusKey, SourceParts, SourceRatingParts, source_statuses}
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{Query, TypedCursor, TypedFields}
import x7c1.wheat.modern.database.WritableDatabase

trait SettingSourceAccessor {
  def findAt(position: Int): Option[SettingSource]
  def length: Int
}

private class SettingSourceAccessorImpl(rawCursor: Cursor) extends SettingSourceAccessor {

  private lazy val cursor = TypedCursor[SettingSourceRecord](rawCursor)

  override def length: Int = rawCursor.getCount

  override def findAt(position: Int): Option[SettingSource] = {
    cursor.moveToFind(position){
      SettingSource(
        sourceId = cursor.source_id,
        accountId = cursor.account_id,
        title = cursor.title,
        description = cursor.description,
        rating = cursor.rating
      )
    }
  }
}

case class SettingSource(
  sourceId: Long,
  accountId: Long,
  title: String,
  description: String,
  rating: Int
)
object SettingSource {
  implicit object id extends HasSourceId[SettingSource]{
    override def toId = _.sourceId
  }
  implicit object key extends HasSourceStatusKey[SettingSource]{
    override def toId = source =>
      source_statuses.Key(
        accountId = source.accountId,
        sourceId = source.sourceId
      )
  }
}

trait SettingSourceRecord extends TypedFields {
  def source_id: Long
  def account_id: Long
  def title: String
  def description: String
  def rating: Int
}

class SettingSourceAccessorFactory(
  db: SQLiteDatabase,
  accountId: Long ){

  def create[A: HasChannelId](channel: A): SettingSourceAccessor = {
    val channelId = implicitly[HasChannelId[A]] toId channel
    val cursor = createCursor(channelId)
    new SettingSourceAccessorImpl(cursor)
  }
  def createCursor(channelId: Long): Cursor = {
    val query = createQuery(channelId)
    db.rawQuery(query.sql, query.selectionArgs)
  }

  def createQuery(channelId: Long) = {
    val sql1 =
      """SELECT
        | s1.source_id AS source_id,
        | s2.account_id AS account_id,
        | s2.rating AS rating
        |FROM channel_source_map AS s1
        | LEFT JOIN source_ratings AS s2
        |   ON s1.source_id = s2.source_id AND s2.account_id = ?
        |WHERE s1.channel_id = ?
      """.stripMargin

    val sql2 =
      s"""SELECT
        | t1._id AS source_id,
        | t1.title AS title,
        | t1.description AS description,
        | t2.account_id AS account_id,
        | t2.rating AS rating
        |FROM sources AS t1
        |INNER JOIN ($sql1) AS t2 ON t1._id = t2.source_id
        |ORDER BY t2.source_id DESC
      """.stripMargin

    Query(sql2, Array(accountId.toString, channelId.toString))
  }
}

object SettingSourceAccessorFactory {
  def apply[A: HasAccountId](
    helper: DatabaseHelper,
    account: A): SettingSourceAccessorFactory = {

    val db = helper.getReadableDatabase
    val accountId = implicitly[HasAccountId[A]] toId account
    new SettingSourceAccessorFactory(db, accountId)
  }
}

class ChannelOwner(db: SQLiteDatabase, channelId: Long, accountId: Long){
  def addSource(source: ChannelSourceParts): Either[SQLException, Long] = {
    WritableDatabase.transaction(db){ writable =>
      val createdAt = Date.current()
      def insertSource() = writable insert SourceParts(
        title = source.title,
        url = source.url,
        description = source.description,
        createdAt = createdAt
      )
      def insertRating(sourceId: Long) = writable insert SourceRatingParts(
        sourceId = sourceId,
        accountId = accountId,
        rating = source.rating,
        createdAt = createdAt
      )
      def insertMap(sourceId: Long) = writable insert ChannelSourceMapParts(
        channelId = channelId,
        sourceId = sourceId,
        createdAt = createdAt
      )
      for {
        sourceId <- insertSource().right
        ratingId <- insertRating(sourceId).right
        _ <- insertMap(sourceId).right
      } yield sourceId
    }

  }
}

class SourceSubscriber(db: SQLiteDatabase, accountId: Long, sourceId: Long){
  def updateRating(rating: Int): Either[SQLException, Long] =
    WritableDatabase.transaction(db){ writable =>
      writable replace SourceRatingParts(
        sourceId = sourceId,
        accountId = accountId,
        rating = rating,
        createdAt = Date.current()
      )
    }
}

case class ChannelSourceParts(
  url: String,
  title: String,
  description: String,
  rating: Int
)
