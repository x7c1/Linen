package x7c1.linen.scene.inspector

import java.net.URL

import android.database.SQLException
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{HasSourceUrl, InspectorActionParts, InspectorLoadingStatus, InspectorSourceParts, SourceRecord}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.inspector.{ActionPieceLoader, InspectorSourceStatus, LatentUrl}
import x7c1.linen.repository.loader.crawling.SourceContentLoader
import x7c1.linen.repository.loader.queueing.{UrlEnclosure, UrlReceiver, UrlTraverser}
import x7c1.linen.repository.source.inspector.InspectorSource
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

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
    case url: SourceActionUrl =>

      helper.selectorOf[InspectorSource] findBy url matches {
        case Right(None) =>
          // not loaded yet
          // SourceContentLoader loadContent url.raw

          SourceContentLoader() loadContent url.raw transform {
            case Right(content) => ???
            case Left(error) => ???
          }

        case Right(Some(source)) =>
          helper.writable update InspectorSourceStatus(
            actionId = url.actionId,
            loadingStatus = InspectorLoadingStatus.LoadingCompleted,
            latentUrl = url.raw,
            discoveredSourceId = Some(source.original._id),
            updatedAt = Date.current()
          )
        case Left(e: SQLException) =>
          Log error format(e.getCause)("[failed]")
      }

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
