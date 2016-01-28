package x7c1.linen.modern.init.updater

import android.content.Context
import x7c1.linen.modern.accessor.{ChannelSourceParts, ChannelOwner, ChannelAccessor, AccountAccessor, LinenOpenHelper}

class PresetFactory (context: Context, helper: LinenOpenHelper){
  def createPreset() = {
    val db = helper.getWritableDatabase
    val Some(channelOwner) = for {
      accountId <- AccountAccessor.findCurrentAccountId(db)
      channelId <- ChannelAccessor.findCurrentChannelId(db, accountId)
    } yield {
      new ChannelOwner(db, channelId, accountId)
    }
    channelOwner addSource ChannelSourceParts(
      url = "http://www.gizmodo.jp/atom.xml",
      title = "ギズモード・ジャパン",
      description = "ガジェット情報満載ブログ",
      rating = 100
    )
    channelOwner addSource ChannelSourceParts(
      url = "http://feed.rssad.jp/rss/gigazine/rss_2.0",
      title = "GIGAZINE",
      description = "日々のあらゆるシーンで役立つ情報を提供するIT系ニュースサイト。毎日更新中。",
      rating = 100
    )
  }
}
