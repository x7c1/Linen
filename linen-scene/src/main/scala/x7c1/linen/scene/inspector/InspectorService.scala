package x7c1.linen.scene.inspector

import android.app.Service
import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.inspector.ActionPageUrl
import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.linen.repository.loader.queueing.{UrlEnclosure, UrlTraverser}
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log

trait InspectorService {
  def inspect(url: ActionPageUrl): Unit
}

object InspectorService {
  def apply(context: Context with ServiceControl): InspectorService =
    ServiceCaller.reify[InspectorService](
      context,
      context getClassOf ServiceLabel.Updater
    )

  def reify(
    service: Service with ServiceControl,
    helper: DatabaseHelper,
    traverser: UrlTraverser[UrlEnclosure, Unit]): InspectorService = {

    new InspectorServiceImpl(service, traverser, helper)
  }
}

private class InspectorServiceImpl(
  service: Service with ServiceControl,
  traverser: UrlTraverser[UrlEnclosure, Unit],
  helper: DatabaseHelper) extends InspectorService {

  override def inspect(url: ActionPageUrl): Unit = {
    Log info s"[init] account:${url.accountId}, url:${url.raw}"

    traverser startLoading url run CrawlerContext atLeft {
      Log error _.message
    }

  }
}
