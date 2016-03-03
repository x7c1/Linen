package x7c1.linen.modern.init.updater

import android.content.Context
import x7c1.linen.modern.accessor.preset.{PresetAccountAccessor, PresetChannelPiece, PresetRecordError}
import x7c1.linen.modern.accessor.{ChannelOwner, ChannelSourceParts, LinenOpenHelper}
import x7c1.wheat.macros.logger.Log

class PresetFactory (context: Context, helper: LinenOpenHelper){

  def setupJapanesePresets() = {
//    val db = helper.getWritableDatabase
//    val Some(channelOwner) = for {
//      accountId <- AccountAccessor.findCurrentAccountId(db)
////      accountId <- PresetAccountAccessor(helper).setupAccountId().right
//      channelId <- ChannelAccessor.findCurrentChannelId(db, accountId)
//    } yield {
//      new ChannelOwner(db, channelId, accountId)
//    }
//    val partsList = Seq(
//      ChannelSourceParts(
//        url = "http://www.gizmodo.jp/atom.xml",
//        title = "ギズモード・ジャパン",
//        description = "ガジェット情報満載ブログ",
//        rating = 100
//      ),
//      ChannelSourceParts(
//        url = "http://feed.rssad.jp/rss/gigazine/rss_2.0",
//        title = "GIGAZINE",
//        description = "日々のあらゆるシーンで役立つ情報を提供するIT系ニュースサイト。毎日更新中。",
//        rating = 100
//      ),
//      ChannelSourceParts(
//        url = "http://wired.jp/feed/",
//        title = "WIRED.jp",
//        description = "未来をつくるIDEAS + INNOVATIONS。最新テクノロジーニュース、気になる人物インタビュー、ガジェット情報、サイエンスの最前線など、「未来のトレンド」を毎日発信。US/UK/ITALIA版WIREDからの翻訳記事をいち早くお届け。日本発、注目の動向を追ったオリジナル記事も満載。",
//        rating = 98
//      )
//    )

    setupChannel(Tech)
  }

  def setupChannel(set: PresetChannelSet) = {
    setupChannelOwner(set.channel) match {
      case Left(error) => Log error error.toString
      case Right(owner) => setupSources(owner, set.sources)
    }
  }

  def setupSources(owner: ChannelOwner, sourcePieces: PresetSourcePieces) = {
    val partsList = sourcePieces.list.map {
      piece => ChannelSourceParts(
        url = piece.url,
        title = piece.title,
        description = s"(not loaded yet)",
        rating = 100
      )
    }
    val addSource: ChannelSourceParts => Unit = parts =>
      owner addSource parts match {
        case Left(e) => Log error s"url:${parts.url} ${e.getMessage}"
        case Right(b) => Log info s"inserted: ${parts.url}"
      }

    partsList foreach addSource
  }
  def setupChannelOwner(
    channelPiece: PresetChannelPiece): Either[PresetRecordError, ChannelOwner] = {

    val accessor = PresetAccountAccessor(helper)
    for {
      account <- accessor.setupAccount().right
      channel <- accessor.setupChannel(account, channelPiece).right
    } yield new ChannelOwner(
      db = helper.getWritableDatabase,
      channelId = channel.channelId,
      accountId = account.accountId
    )
  }
}

case class PresetSourcePiece(
  title: String,
  url: String
)
case class PresetSourcePieces(
  list: Seq[PresetSourcePiece]
)
trait PresetChannelSet {
  def channel: PresetChannelPiece
  def sources: PresetSourcePieces
}

object Tech extends PresetChannelSet {
  override def channel = PresetChannelPiece(
    name = "Tech",
    description = "IT / インターネット / 科学技術 / ガジェット"
  )
  override def sources = PresetSourcePieces(
    list = Seq(
      PresetSourcePiece(
        title = "ギズモード・ジャパン",
        url = "http://www.gizmodo.jp/atom.xml"
      ),
      PresetSourcePiece(
        title = "GIGAZINE",
        url = "http://feed.rssad.jp/rss/gigazine/rss_2.0"
      ),
      PresetSourcePiece(
        title = "WIRED.jp",
        url = "http://wired.jp/feed/"
      ),
      PresetSourcePiece(
        title = "INTERNET Watch",
        url = "http://rss.rssad.jp/rss/internetwatch/internet.rdf"
      ),
      PresetSourcePiece(
        title = "ケータイ Watch",
        url = "http://rss.rssad.jp/rss/k-taiwatch/k-tai.rdf"
      ),
      PresetSourcePiece(
        title = "AV Watch",
        url = "http://rss.rssad.jp/rss/avwatch/av.rdf"
      ),
      PresetSourcePiece(
        title = "PC Watch",
        url = "http://rss.rssad.jp/rss/impresswatch/pcwatch.rdf"
      ),
      PresetSourcePiece(
        title = "ASCII.jp － トップ",
        url = "http://rss.rssad.jp/rss/ascii/rss.xml"
      ),
      PresetSourcePiece(
        title = "ASCII.jp － TECH",
        url = "http://rss.rssad.jp/rss/ascii/it/rss.xml"
      ),
      PresetSourcePiece(
        title = "ASCII.jp － 自作PC",
        url = "http://rss.rssad.jp/rss/ascii/pc/rss.xml"
      ),
      PresetSourcePiece(
        title = "ASCII.jp － アスキークラウド",
        url = "http://ascii.jp/cloud/rss.xml"
      ),
      PresetSourcePiece(
        title = "ASCII.jp － デジタル",
        url = "http://rss.rssad.jp/rss/ascii/digital/rss.xml"
      )
    )
  )
}
object Game extends PresetChannelSet {
  override def channel = PresetChannelPiece(
    name = "Game",
    description = "ゲーム / ホビー"
  )
  override def sources = PresetSourcePieces(
    list = Seq(
      PresetSourcePiece(
        title = "GAME Watch",
        url = "http://rss.rssad.jp/rss/gamewatch/index.rdf"
      ),
      PresetSourcePiece(
        title = "ASCII.jp － ゲーム・ホビー",
        url = "http://rss.rssad.jp/rss/ascii/hobby/rss.xml"
      )
    )
  )
}
