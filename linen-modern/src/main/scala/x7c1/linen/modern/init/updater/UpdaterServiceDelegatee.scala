package x7c1.linen.modern.init.updater

import java.util.concurrent.Executors

import android.app.Service
import android.content.Intent
import android.os.IBinder
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.linen.modern.init.dev.DummyFactory
import x7c1.wheat.macros.intent.{ExtraNotFound, IntentExpander}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.service.CommandStartType
import x7c1.wheat.modern.decorator.service.CommandStartType.NotSticky
import x7c1.wheat.modern.patch.TaskAsync.async

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scalaz.{-\/, \/-, EitherT}

object LinenService {
  object Implicits {
    import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
    private lazy val pool = Executors.newCachedThreadPool()
    implicit def executor: ExecutionContextExecutor = ExecutionContext fromExecutor pool
  }
}

object UpdaterServiceDelegatee {
  val ActionTypeSample = "hoge"
}

class UpdaterServiceDelegatee(service: Service with ServiceControl){

  private lazy val helper = new LinenOpenHelper(service)

  def onBind(intent: Intent): Option[IBinder] = {
    Log info "[init]"
    None
  }

  def onStartCommand(intent: Intent, flags: Int, startId: Int): CommandStartType = {
    Log info s"[init] start:$startId, $intent"

    new UpdaterMethods(service, helper, startId) execute intent
    NotSticky
  }
  def onDestroy(): Unit = {
    Log info "[init]"
    helper.close()
  }
}

class UpdaterMethods(
  service: Service with ServiceControl,
  helper: LinenOpenHelper,
  startId: Int){

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
  def loadSource(sourceId: Long): Unit = {
    Log info s"[init] source-id: $sourceId"

    import LinenService.Implicits._
    val inspector = SourceInspector(helper)

    import scalaz.std.scalaFuture._
    val either = for {
      url <- EitherT fromEither Future { inspector findFeedUrl sourceId }
      entries <- EitherT right inspector.loadEntries(sourceId)(url)
    } yield
      insertEntries(entries)

    either.run onComplete {
      case Success(\/-(_)) => Log info s"[done] source-id:$sourceId"
      case Success(-\/(e)) => Log error e.message
      case Failure(e) => Log error e.getMessage
    }
  }
  private def insertEntries(entries: LoadedEntries): Unit = {
    val loadedEntries = entries.validEntries
    val notifier = new UpdaterServiceNotifier(service, loadedEntries.length)
    loadedEntries.zipWithIndex foreach {
      case (entry, index) =>
        Log info entry.title
        helper.writableDatabase insert entry
        notifier.notifyProgress(index)
    }
    notifier.notifyDone()
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

