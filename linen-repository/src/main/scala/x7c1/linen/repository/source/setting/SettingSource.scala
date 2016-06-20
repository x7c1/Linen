package x7c1.linen.repository.source.setting

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.SettingSourceRecord
import x7c1.linen.database.struct.{ChannelSourceMapParts, HasChannelStatusKey, HasSourceId, HasSourceStatusKey, SourceParts, SourceRatingParts, source_statuses}
import x7c1.linen.repository.date.Date
import x7c1.wheat.modern.database.WritableDatabase
import x7c1.wheat.modern.database.selector.presets.{CanTraverseEntity, TraverseOn}
import x7c1.wheat.modern.database.selector.{CursorConvertible, SelectorProvidable}


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
  implicit object readable extends CursorConvertible[SettingSourceRecord, SettingSource] {
    override def convertFrom = cursor =>
      SettingSource(
        sourceId = cursor.source_id,
        accountId = cursor.account_id,
        title = cursor.title,
        description = cursor.description,
        rating = cursor.rating
      )
  }
  implicit object traverse extends CanTraverseEntity[
    HasChannelStatusKey,
    SettingSourceRecord,
    SettingSource
  ]
  implicit object providable extends SelectorProvidable[SettingSource, Selector]

  class Selector(
    protected val db: SQLiteDatabase) extends TraverseOn[HasChannelStatusKey, SettingSource]
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
