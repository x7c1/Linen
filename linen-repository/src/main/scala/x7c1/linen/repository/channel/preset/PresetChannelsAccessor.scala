package x7c1.linen.repository.channel.preset

import x7c1.linen.database.control.DatabaseHelper
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Query

trait PresetChannelsAccessor {
  def clientAccountId: Long
  def length: Int
  def findAt(position: Int): Option[SettingPresetChannel]
  def reload(): Unit
}


private class PresetChannelAccessorImpl(
  helper: DatabaseHelper,
  query: Query,
  accountId: Long ) extends PresetChannelsAccessor {

  private var (rawCursor, cursor) = init()

  override def length = rawCursor.getCount

  override def findAt(position: Int) = {
    cursor.moveToFind(position){
      SettingPresetChannel(
        channelId = cursor.channel_id,
        accountId = accountId,
        name = cursor.name,
        description = cursor.description,
        isSubscribed = cursor.subscribed == 1
      )
    }
  }
  private def init() = {
    val raw = helper.getReadableDatabase.rawQuery(query.sql, query.selectionArgs)
    val typed = TypedCursor[SettingPresetChannelRecord](raw)
    raw -> typed
  }
  override def reload(): Unit = synchronized {
    init() match { case (raw, typed) =>
      rawCursor.close()
      rawCursor = raw
      cursor = typed
    }
  }
  override def clientAccountId = accountId
}

trait SettingPresetChannelRecord extends TypedFields {
  def channel_id: Long
  def name: String
  def description: String
  def subscribed: Int
}

