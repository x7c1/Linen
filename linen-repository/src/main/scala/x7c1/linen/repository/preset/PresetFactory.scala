package x7c1.linen.repository.preset

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.account.PresetAccount
import x7c1.linen.repository.account.setup.PresetAccountSetup
import x7c1.linen.repository.channel.preset.{PresetChannelSetup, PresetChannelPiece}
import x7c1.linen.repository.source.setting.{ChannelOwner, ChannelSourceParts}
import x7c1.wheat.macros.logger.Log

class PresetFactory (helper: DatabaseHelper){

  def setupJapanesePresets() = {
    val sets = Seq(
      Tech,
      Game,
      Column
    )
    sets.reverse map SetupStarter(helper) foreach {_.start()}
  }

}
private case class SetupStarter(helper: DatabaseHelper)(set: PresetChannelSet){

  def start(): Unit = setupChannelOwner() match {
    case Left(error) => Log error error.detail
    case Right(owner) => setupSources(owner)
  }
  private def setupSources(owner: ChannelOwner) = {
    val partsList = set.sources.list.reverse map {
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
        case Right(b) => Log info s"inserted: ${set.channel.name} ${parts.url}"
      }

    partsList foreach addSource
  }
  private def setupChannelOwner(): Either[PresetRecordError, ChannelOwner] = {
    def setupAccount() = {
      PresetAccountSetup(helper).findOrCreate().left map (UnexpectedError(_))
    }
    def setupChannel(account: PresetAccount) = {
      PresetChannelSetup(helper, account) findOrCreate set.channel
    }
    for {
      account <- setupAccount().right
      channel <- setupChannel(account).right
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
object Column extends PresetChannelSet {
  override def channel: PresetChannelPiece = PresetChannelPiece(
    name = "Column",
    description = "コラム / ブログ / 日記 / 寄稿 / 読み物"
  )
  override def sources: PresetSourcePieces = PresetSourcePieces(
    list = Seq(
      PresetSourcePiece(
        title = "Newsweek コラム＆ブログ",
        url = "http://www.newsweekjapan.jp/column/rss.xml"
      )
    )
  )
}