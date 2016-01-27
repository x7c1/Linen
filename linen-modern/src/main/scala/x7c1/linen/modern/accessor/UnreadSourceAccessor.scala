package x7c1.linen.modern.accessor

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.{Date, UnreadSource}
import x7c1.wheat.macros.database.TypedCursor

import scala.util.Try

trait UnreadSourceAccessor {

  def sourceIds: Seq[Long] = {
    (0 to length - 1).map(findAt).flatMap(_.map(_.id))
  }

  def findAt(position: Int): Option[UnreadSource]

  def length: Int

  def positionOf(sourceId: Long): Option[Int]
}

private class UnreadSourceAccessorImpl(rawCursor: Cursor) extends UnreadSourceAccessor {
  private lazy val cursor = TypedCursor[UnreadSourceColumn](rawCursor)

  override def findAt(position: Int) = synchronized {
    cursor.moveToFind(position){
      UnreadSource(
        id = cursor.source_id,
        url = "dummy",
        title = cursor.title,
        description = s"rating:${cursor.rating}-" +  cursor.description,
        rating = cursor.rating,
        startEntryId = cursor.start_entry_id
      )
    }
  }
  override def length = {
    rawCursor.getCount
  }
  override def positionOf(sourceId: Long): Option[Int] = {
    (0 to length - 1) find { n =>
      findAt(n).exists(_.id == sourceId)
    }
  }
}

class UnreadSourceAccessorFactory(db: SQLiteDatabase){
  def create(channelId: Long, accountId: Long): UnreadSourceAccessor = {
    val cursor = UnreadSourceAccessor.createCursor(db, channelId, accountId)
    new UnreadSourceAccessorImpl(cursor)
  }
}

object UnreadSourceAccessor {
  def create(
    db: SQLiteDatabase,
    accountId: Long, channelId: Long): Try[UnreadSourceAccessor] = {

    Try {
      val cursor = UnreadSourceAccessor.createCursor(db, channelId, accountId)
      new UnreadSourceAccessorImpl(cursor)
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
        |WHERE s1.channel_id = ? AND (s2.account_id IS NULL OR s2.account_id = ?)
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

trait UnreadSourceColumn extends TypedCursor {
  def source_id: Long
  def title: String
  def description: String
  def rating: Int
  def start_entry_id: Option[Long]
}

trait SourceRecordColumn extends TypedCursor {
  def _id: Long
  def title: String
  def description: String
  def url: String
  def created_at: Int --> Date
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
      val column = TypedCursor.expose[SourceRecordColumn]
      TypedCursor toContentValues (
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

trait ChannelSourceMapColumn extends TypedCursor {
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
      val column = TypedCursor.expose[ChannelSourceMapColumn]
      TypedCursor toContentValues (
        column.source_id -> target.sourceId,
        column.channel_id -> target.channelId,
        column.created_at -> target.createdAt
      )
    }
  }
}


/*
object channel_source_map extends Table(name = "channel_source_map"){
  trait Record extends TypedField {
    def source_id: Long
    def channel_id: Long
    def created_at: Int --> Date
  }
  case class Key extends TypedKey (
    def source_id: Long,
    def channel_id: Long
  )
  object Record {

    implicit def single: SingleSelectable[Record, Key] =
      TypedField.toSingleSelectable(channel_source_map.name)

    // is expanded like:

    implicit def single: SingleSelectable[Record, Key] = new SingleSelectable[Record, Key]{
      override def tableName: String = channel_source_map.tableName

      override def fromCursor(cursor: android.database.Cursor): Option[Record] = {
        val row = TypedCursor[Record](cursor)
        row.moveToFind(0){
          new Record {
            override val source_id: Long = row.source_id
            override val channel_id: Long = row.channel_id
            override val created_at: Int --> Date = row.created_at
          }
        }
      }
      override def where(key: Key): Seq[(String, String)] = Seq(
        "channel_id" -> key.channel_id,
        "source_id" -> key.source_id
      )
    }
    // can be used like:
    database.selectOne[channel_source_map.Record](channel_source_map.Key(
      source_id = 111,
      channel_id = 222
    ))

  }

  trait Selector {
    def findBy(source_id: Long, channel_id: Long): Option[Record]
  }
  def apply(db: ReadableDatabase): Selector = TypedCursor.selectorOf[Selector]

  // is expanded as:

  new Selector {
    override def findBy(source_id: Long, channel_id: Long): Option[Record] = {
      val selectable = new SingleSelectable {
        override def tableName = channel_source_map.this.tableName
        override def where(id: (Long, Long)) = Seq(
          "source_id" -> id._1,
          "channel_id" -> id._2
        )
        override def fromCursor(cursor: Cursor) = new Record with TypedCursor {
          ...
        }
      }
      db.selectOne[Record].apply(source_id, channel_id)(selectable)
    }
  }

  // usage
  channel_source_map(database).findBy(
    source_id = 111,
    channel_id = 222
  )
}
*/
