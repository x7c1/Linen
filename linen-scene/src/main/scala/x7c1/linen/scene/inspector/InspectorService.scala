package x7c1.linen.scene.inspector

import android.app.Service
import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{InspectorActionParts, InspectorLoadingStatus, InspectorSourceParts}
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.inspector.{ActionPiece, ActionPieceLoader, LatentUrl}
import x7c1.linen.repository.loader.crawling.{CrawlerContext, CrawlerFate}
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.fate.FutureFate

trait InspectorService {
  def inspect(accountId: Long, pageUrl: String): Unit
}

object InspectorService {
  def apply(context: Context with ServiceControl): InspectorService =
    ServiceCaller.reify[InspectorService](
      context,
      context getClassOf ServiceLabel.Updater
    )

  def reify(
    service: Service with ServiceControl,
    helper: DatabaseHelper): InspectorService = {

    new InspectorServiceImpl(service, helper)
  }
}

private class InspectorServiceImpl(
  service: Service with ServiceControl,
  helper: DatabaseHelper) extends InspectorService {

  private val provide = FutureFate.hold[CrawlerContext, InspectorServiceError]

  override def inspect(accountId: Long, pageUrl: String): Unit = {
    Log info s"[init]"
    val fate = provide right {
      val piece = ActionPieceLoader loadFrom pageUrl
      val actionParts = InspectorActionParts(
        loadingStatus = InspectorLoadingStatus.Loading,
        accountId = accountId,
        originTitle = piece.originTitle,
        originUrl = piece.originUrl,
        createdAt = Date.current(),
        updatedAt = Date.current()
      )
      helper.writable insert actionParts match {
        case Left(e) =>
          Log error e.getMessage
        case Right(actionId) =>
          piece.latentUrls map toSource(actionId) foreach insertParts
      }
    }
    fate run CrawlerContext atLeft {
      Log error _.message
    }
  }

  private def insertParts(parts: InspectorSourceParts) = {
    helper.writable insert parts match {
      case Left(e) => Log error e.getMessage
      case Right(_) => // nop
    }
  }

  private def toSource(actionId: Long): LatentUrl => InspectorSourceParts = {
    latentUrl =>
      InspectorSourceParts(
        actionId = actionId,
        loadingStatus = InspectorLoadingStatus.Loading,
        latentUrl = latentUrl.full,
        discoveredSourceId = None,
        createdAt = Date.current(),
        updatedAt = Date.current()
      )
  }
}
