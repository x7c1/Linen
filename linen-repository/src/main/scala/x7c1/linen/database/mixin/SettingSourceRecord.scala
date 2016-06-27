package x7c1.linen.database.mixin

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasChannelStatusKey
import x7c1.wheat.macros.database.{Query, TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.{CanTraverseRecord, TraverseOn}
import x7c1.wheat.modern.database.selector.{RecordReifiable, SelectorProvidable}

trait SettingSourceRecord extends TypedFields {
  def source_id: Long
  def account_id: Long
  def title: String
  def description: String
  def rating: Int
}

object SettingSourceRecord {

  def column = TypedFields.expose[SettingSourceRecord]

  implicit object providable extends SelectorProvidable[SettingSourceRecord, Selector]

  implicit object reifiable extends RecordReifiable[SettingSourceRecord]{
    override def reify(cursor: Cursor) = TypedCursor[SettingSourceRecord](cursor)
  }
  implicit object traverse extends CanTraverseRecord[HasChannelStatusKey, SettingSourceRecord]{
    override def queryAbout[X: HasChannelStatusKey](target: X): Query = {
      val key = implicitly[HasChannelStatusKey[X]] toId target
      val accountId = key.accountId
      val channelId = key.channelId
      val sql1 =
        """SELECT
          | s1.source_id AS source_id,
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
          | $accountId AS account_id,
          | t2.rating AS rating
          |FROM sources AS t1
          |INNER JOIN ($sql1) AS t2 ON t1._id = t2.source_id
          |ORDER BY t2.source_id DESC
      """.stripMargin

      Query(sql2, Array(accountId.toString, channelId.toString))
    }
  }
  class Selector(
    protected val db: SQLiteDatabase) extends TraverseOn[HasChannelStatusKey, SettingSourceRecord]
}
