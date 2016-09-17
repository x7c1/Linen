package x7c1.linen.scene.inspector

import java.net.URL

import android.database.SQLException
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{HasSourceId, InspectorActionParts, InspectorLoadingStatus, InspectorSourceParts, SourceParts}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.inspector.{ActionPieceLoader, InspectorSourceStatus, LatentUrl}
import x7c1.linen.repository.loader.crawling.LoadedEntry.toEntryParts
import x7c1.linen.repository.loader.crawling.{CrawlerContext, InvalidEntry, LoadedEntry, SourceContentLoader}
import x7c1.linen.repository.loader.queueing.{UrlEnclosure, UrlReceiver, UrlTraverser}
import x7c1.linen.repository.source.inspector.InspectorSource
import x7c1.wheat.macros.logger.Log
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
      Log info s"[piece] $piece"

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
          Log error format(e.getCause)("[failed]")
        case Right(actionId) =>
          piece.latentUrls map toSourceParts(actionId) foreach insertInspectorSourceParts
      }
  }

  val startSourceAction: UrlReceiver = UrlReceiver {
    case url: SourceActionUrl =>
      helper.selectorOf[InspectorSource] findBy url matches {
        case Right(None) =>
          startLoading(url)
        case Right(Some(source)) =>
          updateStatus(url, source)
        case Left(e: SQLException) =>
          Log error format(e.getCause)("[failed]")
      }
  }

  private def updateStatus[A: HasSourceId](url: SourceActionUrl, source: A): Unit = {
    Log info s"[init] $url"

    helper.writable update InspectorSourceStatus(
      actionId = url.actionId,
      loadingStatus = InspectorLoadingStatus.LoadingCompleted,
      latentUrl = url.raw,
      discoveredSourceId = Some(implicitly[HasSourceId[A]] toId source),
      updatedAt = Date.current()
    ) match {
      case Left(e) => Log error format(e.getCause)("[failed]")
      case Right(rows) => Log info s"[done] $url"
    }
  }

  private def startLoading(url: SourceActionUrl): Unit = {
    Log info s"[init] $url"

    SourceContentLoader() loadContent url.raw run CrawlerContext apply {
      case Right(content) =>
        Log info s"content: $content"

        helper.writable insert SourceParts(
          title = content.title,
          url = url.raw.toExternalForm,
          description = content.description,
          createdAt = Date.current()
        ) match {
          case Right(sourceId) =>
            Log info s"content-entries: ${content.entries.length}"

            updateStatus(url, sourceId)
            content.entries foreach insertEntry(sourceId)
          case Left(e) =>
            Log error format(e.getCause)("[failed]")
        }
      case Left(error) =>
        Log error error.detail
    }
  }

  private def insertEntry(sourceId: Long)(either: Either[InvalidEntry, LoadedEntry]): Unit = {
    Log info s"[init] $either"

    either match {
      case Right(entry) => helper.writable insert toEntryParts(sourceId)(entry) match {
        case Right(id) => //nop
        case Left(e) => Log error format(e.getCause)("[failed]")
      }
      case Left(invalid) => Log error invalid.detail
    }
  }

  private def insertInspectorSourceParts(parts: InspectorSourceParts) = {
    Log info s"[init] ${parts.latentUrl}"

    helper.writable insert parts match {
      case Right(_) =>
        getTraverser() startLoading SourceActionUrl(
          actionId = parts.actionId,
          raw = new URL(parts.latentUrl)
        ) run CrawlerContext atLeft {
          Log error _.message
        }
      case Left(e) =>
        Log error format(e.getCause)("[failed]")
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
