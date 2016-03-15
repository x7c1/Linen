package x7c1.linen.modern.accessor.setting

import x7c1.linen.modern.accessor.preset.{NoPresetAccount, PresetAccount, PresetRecordError, UnexpectedException}
import x7c1.linen.modern.accessor.{LinenOpenHelper, Query}
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}

trait PresetChannelsAccessor {
  def length: Int
  def findAt(position: Int): Option[SettingPresetChannel]
  def reload(): Unit
}

object PresetChannelsAccessor {
  def create(
    clientAccountId: Long,
    helper: LinenOpenHelper): Either[PresetRecordError, PresetChannelsAccessor] = {

    val presetAccount = helper.readable.find[PresetAccount]()
    val either = presetAccount match {
      case Left(error) => Left(UnexpectedException(error))
      case Right(None) => Left(NoPresetAccount())
      case Right(Some(preset)) => Right(preset.accountId)
    }
    either.right map { presetAccountId =>
      val query = createQuery(clientAccountId, presetAccountId)
      new PresetChannelAccessorImpl(helper, query)
    }
  }
  def createQuery(clientAccountId: Long, presetAccountId: Long) = {
    val sql =
      s"""SELECT
         |  c1._id AS channel_id,
         |  c1.name AS name,
         |  c1.description AS description,
         |  IFNULL(c2.subscribed, 0) AS subscribed
         |FROM channels AS c1
         |  LEFT JOIN channel_statuses AS c2
         |    ON c2.account_id = ? AND c1._id = c2.channel_id
         |WHERE c1.account_id = ?
         |ORDER BY c1._id DESC
       """.stripMargin

    new Query(sql, Array(
      clientAccountId.toString,
      presetAccountId.toString)
    )
  }
}

private class PresetChannelAccessorImpl(helper: LinenOpenHelper, query: Query) extends PresetChannelsAccessor {

  private var (rawCursor, cursor) = init()

  override def length = rawCursor.getCount

  override def findAt(position: Int) = {
    cursor.moveToFind(position){
      SettingPresetChannel(
        channelId = cursor.channel_id,
        name = cursor.name,
        description = cursor.description,
        isSubscribed = cursor.subscribed == 1
      )
    }
  }
  def init() = {
    val raw = helper.getReadableDatabase.rawQuery(query.sql, query.selectionArgs)
    val typed = TypedCursor[SettingPresetChannelRecord](raw)
    raw -> typed
  }
  override def reload(): Unit = synchronized {
    init() match { case (raw, typed) =>
      rawCursor = raw
      cursor = typed
    }
  }
}

trait SettingPresetChannelRecord extends TypedFields {
  def channel_id: Long
  def name: String
  def description: String
  def subscribed: Int
}

case class SettingPresetChannel(
  channelId: Long,
  name: String,
  description: String,
  isSubscribed: Boolean
)
