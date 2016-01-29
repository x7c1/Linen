package x7c1.linen.modern.init.updater

import java.io.{BufferedInputStream, InputStreamReader}
import java.net.{HttpURLConnection, URL}

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.io.SyndFeedInput
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.accessor.{EntryParts, LinenOpenHelper, SourceRecordColumn}
import x7c1.linen.modern.init.dev.DummyFactory
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.intent.{ExtraNotFound, IntentExpander}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.using
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky
import x7c1.wheat.modern.patch.TaskAsync.async

import scalaz.{-\/, \/, \/-}

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

        try loadEntries(source)(insertEntries)
        catch {
          case e: Exception => Log error e.getMessage
        }

      case Right(none) =>
      case Left(exception) =>
    }

    service stopSelfResult startId
  }
  private def insertEntries(entries: Seq[EntryNotLoaded \/ EntryParts]): Unit =
    entries foreach {
      case \/-(entry) =>
        Log info entry.url
        Log info entry.title
        Log info entry.content
        helper.writableDatabase insert entry
      case -\/(empty) =>
    }

  private def loadEntries
    (source: SourceRecordColumn): CallbackTask[Seq[EntryNotLoaded \/ EntryParts]] = {

    val sourceUrl = source.url
    Log info s"[init] $sourceUrl"

    val feedUrl = new URL(sourceUrl)
    val connection = feedUrl.openConnection().asInstanceOf[HttpURLConnection]
    connection setRequestMethod "GET"

    Log info s"code:${connection.getResponseCode}"

    import scala.collection.JavaConversions._
    for {
      stream <- using(new BufferedInputStream(connection.getInputStream))
      reader <- using(new InputStreamReader(stream))
    } yield {
      val feed = new SyndFeedInput().build(reader)

      Log error feed.getTitle
      Log info feed.getDescription

      feed.getEntries map { case entry: SyndEntry =>
        convertEntry(source)(entry)
      }
    }

  }

  private def convertEntry
    (source: SourceRecordColumn)(entry: SyndEntry): EntryNotLoaded \/ EntryParts = {
    import scalaz.\/.left
    import scalaz.syntax.std.option._

    try for {
      url <- Option(entry.getLink) \/> EmptyUrl()
      published <- Option(entry.getPublishedDate) \/> EmptyPublishedDate()
    } yield EntryParts(
      sourceId = source._id,
      title = Option(entry.getTitle) getOrElse "",
      content = Option(entry.getDescription.getValue) getOrElse "",
      url = url,
      createdAt = Date(published)
    ) catch {
      case e: Exception => left apply Abort(e)
    }
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

