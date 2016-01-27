package x7c1.linen.modern.accessor

import android.database.sqlite.SQLiteDatabase
import android.database.{Cursor, SQLException}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.database.TypedCursor

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
        title = cursor.title,
        description = cursor.description,
        rating = cursor.rating
      )
    }
  }
}

case class SettingSource(
  sourceId: Long,
  title: String,
  description: String,
  rating: Int
)

trait SettingSourceRecord extends TypedCursor {
  def source_id: Long
  def title: String
  def description: String
  def rating: Int
}

class SettingSourceAccessorFactory(
  db: SQLiteDatabase,
  accountId: Long ){

  def create(channelId: Long): SettingSourceAccessor = {
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
        | s2.rating AS rating
        |FROM channel_source_map AS s1
        |INNER JOIN source_ratings AS s2 ON s1.source_id = s2.source_id
        |WHERE s1.channel_id = ? AND s2.owner_account_id = ?
      """.stripMargin

    val sql2 =
      s"""SELECT
        | t1._id AS source_id,
        | t1.title AS title,
        | t1.description AS description,
        | t2.rating AS rating
        |FROM sources AS t1
        |INNER JOIN ($sql1) AS t2 ON t1._id = t2.source_id
        |ORDER BY t2.source_id DESC
      """.stripMargin

    new Query(sql2, Array(channelId.toString, accountId.toString))
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
        ownerAccountId = accountId,
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
        ownerAccountId = accountId,
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
