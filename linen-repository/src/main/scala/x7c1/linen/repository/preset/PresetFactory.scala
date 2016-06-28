package x7c1.linen.repository.preset

import com.typesafe.config.ConfigFactory
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.account.PresetAccount
import x7c1.linen.repository.account.setup.PresetAccountSetup
import x7c1.linen.repository.channel.preset.{PresetChannelPiece, PresetChannelSetup}
import x7c1.linen.repository.source.setting.{ChannelOwner, ChannelSourceParts}
import x7c1.wheat.macros.logger.Log

import scala.collection.JavaConverters._

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
        description = "",
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
object PresetSourcePieces {
  private def sourceList(file: String) = {
    val config = ConfigFactory.parseResources(
      getClass.getClassLoader,
      file
    )
    config.getObjectList("sources").asScala map (_.toConfig)
  }
  def from(file: String) = PresetSourcePieces(
    list = sourceList(file) map { conf =>
      PresetSourcePiece(
        title = conf getString "title",
        url = conf getString "url"
      )
    }
  )

}
trait PresetChannelSet {
  def channel: PresetChannelPiece
  def sources: PresetSourcePieces
}

object Tech extends PresetChannelSet {
  override def channel = PresetChannelPiece(
    name = "Tech",
    description = "IT / インターネット / 科学技術 / ガジェット"
  )
  override def sources = {
    PresetSourcePieces from "preset-tech-jp.json"
  }
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
