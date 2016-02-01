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
    channelOwner addSource ChannelSourceParts(
      url = "http://wired.jp/feed/",
      title = "WIRED.jp",
      description = "未来をつくるIDEAS + INNOVATIONS。最新テクノロジーニュース、気になる人物インタビュー、ガジェット情報、サイエンスの最前線など、「未来のトレンド」を毎日発信。US/UK/ITALIA版WIREDからの翻訳記事をいち早くお届け。日本発、注目の動向を追ったオリジナル記事も満載。",
      rating = 98
    )
  }
}
