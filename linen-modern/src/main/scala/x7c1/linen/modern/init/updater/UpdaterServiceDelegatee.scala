package x7c1.linen.modern.init.updater

import java.io.{BufferedInputStream, BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL}

import android.app.Service
import android.content.{Context, Intent}
import android.os.IBinder
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.io.{SyndFeedInput, XmlReader}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.accessor.{EntryParts, AccountAccessor, ChannelAccessor, ChannelOwner, ChannelSourceParts, LinenOpenHelper, SourceRecordColumn}
import x7c1.linen.modern.init.dev.DummyFactory
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.intent.{ExtraNotFound, IntentExpander}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky
import x7c1.wheat.modern.patch.TaskAsync.async

import scala.annotation.tailrec

object UpdaterServiceDelegatee {
  val ActionTypeSample = "hoge"
}

class UpdaterServiceDelegatee(service: Service with ServiceControl){

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }

  def onStartCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info s"[init] start:$startId, $intent"

    new UpdaterMethods(service, startId) execute intent

    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
  }
}
class UpdaterMethods(service: Service with ServiceControl, startId: Int){
  private lazy val helper = new LinenOpenHelper(service)

  def execute(intent: Intent) = IntentExpander findFrom intent match {
    case Left(e: ExtraNotFound) => Log error e.toString
    case Left(notFound) => Log info notFound.toString
    case Right(f) => f.apply()
  }

  def createDummies(max: Int): Unit = async {
    Log info "[init]"
    val notifier = new UpdaterServiceNotifier(service, max)
    DummyFactory.createDummies0(service)(max){ n =>
      notifier.notifyProgress(n)
    }
    notifier.notifyDone()
    service stopSelf startId
  }
  def createPreset(): Unit = async {
    Log info "[init]"

    new PresetFactory(service, helper).createPreset()
  }
  def loadSource(sourceId: Long): Unit = async {
    Log error s"[init] source-id: $sourceId"

    helper.readable.selectOne[SourceRecordColumn](sourceId) match {
      case Right(Some(source)) =>
        Log info source.title
        loadEntries(source.url) foreach { entry =>
          Log info entry.getUri
          helper.writableDatabase insert EntryParts(
            sourceId = sourceId,
            title = Option(entry.getTitle) getOrElse "",
            content = entry.getDescription.getValue,
            url = entry.getLink,
            createdAt = Date(entry.getPublishedDate)
          )
        }

      case Right(none) =>
      case Left(exception) =>
    }

    service stopSelfResult startId
  }
  private def loadEntries(url: String): Seq[SyndEntry] = {
    Log info s"[init] $url"

    val url2 = new URL(url)
    val connection = url2.openConnection().asInstanceOf[HttpURLConnection]
    connection setRequestMethod "GET"

    Log info s"code:${connection.getResponseCode}"
    val stream = new BufferedInputStream(connection.getInputStream)
    val reader = new BufferedReader(new InputStreamReader(stream))
    val lines = SampleReader(reader) read { line =>
      line
    }
    Log info lines.mkString("\n")
    lines foreach { line =>
      Log info line
    }
    Log error s"lines:${lines.length}"

    val input2 = new SyndFeedInput()
    val feed2 = input2.build(new XmlReader(url2))
    Log error feed2.getTitle
    Log info feed2.getDescription

    import scala.collection.JavaConversions._
    feed2.getEntries map { case entry: SyndEntry =>
      Log error entry.getTitle
      Log info entry.getDescription.getValue
      entry
    }
  }
}

object SampleReader {
  def apply(reader: BufferedReader): SampleReader = new SampleReader(reader)
}
class SampleReader private (reader: BufferedReader){
  def read[A](f: String => A): Seq[A] = {
    @tailrec
    def loop(xs: Seq[A]): Seq[A] = Option(reader.readLine()) match {
      case Some(line) =>
        loop(xs :+ f(line))
      case None =>
        xs
    }
    try loop(Seq())
    finally reader.close()
  }
}

/*
case class Hoge123(name: String)

object Hoge123 {
  implicit object convertible extends BundleConvertible[Hoge123] {
    override def toBundle(target: Hoge123): Bundle = {
      val x = new Bundle()
      x.putString("name", target.name)
      x
    }
  }
}
*/

class PresetFactory (context: Context, helper: LinenOpenHelper){
  def createPreset() = {
    val db = helper.getWritableDatabase
    val writable = helper.writableDatabase

    val Some(accountId) = AccountAccessor.findCurrentAccountId(db)
    val Some(channelId) = ChannelAccessor.findCurrentChannelId(db, accountId)

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
  }
}
