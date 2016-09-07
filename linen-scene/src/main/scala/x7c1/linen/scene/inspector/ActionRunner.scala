package x7c1.linen.scene.inspector

import java.net.URL

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{InspectorActionParts, InspectorLoadingStatus, InspectorSourceParts}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.inspector.{ActionPieceLoader, LatentUrl}
import x7c1.linen.repository.loader.queueing.{UrlEnclosure, UrlReceiver, UrlTraverser}
import x7c1.wheat.macros.logger.Log

object ActionRunner {
  def apply(
    helper: DatabaseHelper,
    getTraverser: () => UrlTraverser[UrlEnclosure, Unit]): ActionRunner = {

    new ActionRunner(helper, getTraverser)
  }
}

class ActionRunner private(
  helper: DatabaseHelper,
  getTraverser: () => UrlTraverser[UrlEnclosure, Unit]) {

  val startPageAction: UrlReceiver = UrlReceiver {
    case url: ActionPageUrl =>
      val piece = ActionPieceLoader loadFrom url.raw.toString
      val actionParts = InspectorActionParts(
        loadingStatus = InspectorLoadingStatus.Loading,
        accountId = url.accountId,
        originTitle = piece.originTitle,
        originUrl = piece.originUrl,
        createdAt = Date.current(),
        updatedAt = Date.current()
      )
      helper.writable insert actionParts match {
        case Left(e) =>
          Log error e.getMessage
        case Right(actionId) =>
          piece.latentUrls map toSourceParts(actionId) foreach insertParts
      }
  }

  val startSourceAction: UrlReceiver = UrlReceiver {
    case n: SourceActionUrl =>
      ???
  }

  private def insertParts(parts: InspectorSourceParts) = {
    helper.writable insert parts match {
      case Right(_) =>
        getTraverser() startLoading SourceActionUrl(
          actionId = parts.actionId,
          raw = new URL(parts.latentUrl)
        )
      case Left(e) =>
        Log error e.getMessage
    }
  }

  private def toSourceParts(actionId: Long): LatentUrl => InspectorSourceParts = {
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

case class ActionPageUrl(
  accountId: Long,
  override val raw: URL) extends UrlEnclosure

case class SourceActionUrl(
  actionId: Long,
  override val raw: URL) extends UrlEnclosure
