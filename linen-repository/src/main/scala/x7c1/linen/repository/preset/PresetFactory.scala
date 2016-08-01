package x7c1.linen.repository.preset

import com.typesafe.config.{Config, ConfigFactory}
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
       "preset-tech-jp.json"
      ,"preset-game-jp.json"
      ,"preset-column-jp.json"
    ) map PresetChannelSet.fromFile

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

trait PresetChannelSet {
  def channel: PresetChannelPiece
  def sources: PresetSourcePieces
}

object PresetChannelSet {
  def fromFile(fileName: String): PresetChannelSet = {
    val config = ConfigFactory.parseResources(
      getClass.getClassLoader,
      fileName
    )
    new PresetChannelSetImpl(config)
  }
  private class PresetChannelSetImpl(config: Config) extends PresetChannelSet {
    val list = config.getObjectList("sources").asScala map (_.toConfig)

    override def channel: PresetChannelPiece = PresetChannelPiece(
      name = config getString "name",
      description = config getString "description"
    )
    override def sources: PresetSourcePieces = PresetSourcePieces(
      list = list map { conf =>
        PresetSourcePiece(
          title = conf getString "title",
          url = conf getString "url"
        )
      }
    )
  }
}
