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
