package x7c1.linen.modern.accessor

import android.database.{SQLException, Cursor}
import android.database.sqlite.SQLiteDatabase
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
        title = cursor.title
      )
    }
  }
}

case class SettingSource(
  sourceId: Long,
  title: String
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
    val sql1 =
      """SELECT
        | channel_source_map.source_id,
        | title,
        | description,
        | rating
        |FROM channel_source_map
        |INNER JOIN sources ON sources._id = channel_source_map.source_id
        |INNER JOIN source_ratings ON sources._id = source_ratings.source_id
        |WHERE channel_id = ? AND source_ratings.owner_account_id = ?
        |ORDER BY sources._id DESC
      """.stripMargin

    db.rawQuery(sql1, Array(channelId.toString, accountId.toString))
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
