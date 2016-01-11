package x7c1.linen.modern.accessor

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.{Cursor, SQLException}
import x7c1.linen.modern.struct.{Date, Source}

trait SourceAccessor {

  def sourceIds: Seq[Long] = {
    (0 to length - 1).map(findAt).flatMap(_.map(_.id))
  }

  def findAt(position: Int): Option[Source]

  def length: Int

  def positionOf(sourceId: Long): Option[Int]
}

private class SourceAccessorImpl(cursor: Cursor) extends SourceAccessor {

  private lazy val idIndex = cursor getColumnIndex "source_id"
  private lazy val titleIndex = cursor getColumnIndex "title"
  private lazy val descriptionIndex = cursor getColumnIndex "description"
  private lazy val startEntryIdIndex = cursor getColumnIndex "start_entry_id"

  override def findAt(position: Int) = synchronized {
    if (cursor moveToPosition position){
      Some apply Source(
        id = cursor.getLong(idIndex),
        url = "dummy",
        title = cursor.getString(titleIndex),
        description = cursor.getString(descriptionIndex),
        startEntryId = {
          /*
            cannot use cursor.getLong here
              because it returns 0 when target value is null
           */
          Option(cursor getString startEntryIdIndex).map(_.toLong)
        }
      )
    } else None
  }
  override def length = {
    cursor.getCount
  }
  override def positionOf(sourceId: Long): Option[Int] = {
    (0 to length - 1) find { n =>
      findAt(n).exists(_.id == sourceId)
    }
  }

}

class SourceAccessorFactory(db: SQLiteDatabase){
  def create(channelId: Long, accountId: Long): SourceAccessor = {
    val cursor = SourceAccessor.createCursor(db, channelId, accountId)
    new SourceAccessorImpl(cursor)
  }
}

object SourceAccessor {
  def create(
    db: SQLiteDatabase,
    accountId: Long,
    channelId: Long): Either[SqlError, SourceAccessor] = {

    try {
      val cursor = SourceAccessor.createCursor(db, channelId, accountId)
      Right apply new SourceAccessorImpl(cursor)
    } catch {
      case e: SQLException => Left apply SqlError(e)
    }
  }
  def createCursor(db: SQLiteDatabase, channelId: Long, accountId: Long) = {
    val sql1 =
      """SELECT
        |   source_id,
        |   Max(_id) as latest_entry_id
        |FROM entries
        |GROUP BY source_id
      """.stripMargin

    val sql2 =
      """SELECT
        |  s1.source_id,
        |  s1.channel_id,
        |  s2.start_entry_id
        |FROM channel_source_map AS s1
        |LEFT JOIN source_statuses AS s2 ON s1.source_id = s2.source_id
        |WHERE s1.channel_id = ? AND s2.account_id = ?
      """.stripMargin

    val sql3 =
      s"""SELECT
        |  t1.source_id AS source_id,
        |  t1.start_entry_id AS start_entry_id,
        |  t2.latest_entry_id AS latest_entry_id
        |FROM ($sql2) AS t1
        |INNER JOIN ($sql1) AS t2 ON t1.source_id = t2.source_id
        |WHERE t2.latest_entry_id > IFNULL(t1.start_entry_id, 0)
      """.stripMargin

    val sql4 =
      s"""SELECT
        |  u1.source_id AS source_id,
        |  u2.title AS title,
        |  u2.description AS description,
        |  u1.latest_entry_id AS latest_entry_id,
        |  u1.start_entry_id AS start_entry_id
        |FROM ($sql3) as u1
        |INNER JOIN sources as u2 ON u1.source_id = u2._id
       """.stripMargin

    val sql5 =
      s"""SELECT
        |  p4.source_id AS source_id,
        |  p4.title AS title,
        |  p4.description AS description,
        |  p4.start_entry_id AS start_entry_id,
        |  p4.latest_entry_id AS latest_entry_id,
        |  p1.rating AS rating
        |FROM source_ratings AS p1
        |INNER JOIN ($sql4) AS p4 ON p1.source_id = p4.source_id
        |WHERE p1.owner_account_id = ?
        |ORDER BY p1.rating DESC, p1.source_id DESC
       """.stripMargin

    db.rawQuery(sql5,
      Array(channelId.toString, accountId.toString, accountId.toString))
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
      val values = new ContentValues()
      values.put("title", target.title)
      values.put("url", target.url)
      values.put("description", target.description)
      values.put("created_at", target.createdAt.timestamp: java.lang.Integer)
      values
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
  ownerAccountId: Long,
  rating: Int,
  createdAt: Date
)
object SourceRatingParts {
  implicit object insertable extends Insertable[SourceRatingParts] {
    override def tableName: String = "source_ratings"
    override def toContentValues(target: SourceRatingParts): ContentValues = {
      val values = new ContentValues()
      values.put("source_id", target.sourceId: java.lang.Long)
      values.put("owner_account_id", target.ownerAccountId: java.lang.Long)
      values.put("rating", target.rating: java.lang.Integer)
      values.put("created_at", target.createdAt.timestamp: java.lang.Integer)
      values
    }
  }
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
      val values = new ContentValues()
      values.put("source_id", target.sourceId: java.lang.Long)
      values.put("channel_id", target.channelId: java.lang.Long)
      values.put("created_at", target.createdAt.timestamp: java.lang.Integer)
      values
    }
  }
}
