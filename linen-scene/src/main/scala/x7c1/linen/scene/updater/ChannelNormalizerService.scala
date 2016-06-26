package x7c1.linen.scene.updater

import android.app.Service
import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.{ServiceControl, ServiceLabel}
import x7c1.linen.repository.channel.order.ChannelOrderNormalizer
import x7c1.linen.repository.loader.crawling.CrawlerFate
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.macros.reify.New
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

trait ChannelNormalizerService {
  def normalizeRanks(accountId: Long): Unit
}

object ChannelNormalizerService {
  def apply(context: Context with ServiceControl) =
    ServiceCaller.reify[ChannelNormalizerService](
      context,
      context getClassOf ServiceLabel.Updater
    )

  def reify(
    service: Service with ServiceControl,
    helper: DatabaseHelper ): ChannelNormalizerService = {

    new ChannelNormalizerServiceImpl(service, helper)
  }
}

private class ChannelNormalizerServiceImpl(
  service: Service with ServiceControl,
  helper: DatabaseHelper ) extends ChannelNormalizerService {

  override def normalizeRanks(accountId: Long): Unit = CrawlerFate run {
    val updater = New[ChannelOrderNormalizer](helper.getWritableDatabase)
    val either = updater normalizeRanksOf accountId
    either match {
      case Right(num) =>
        Log info s"order updated ($num rows)"
      case Left(e) =>
        Log error format(e){"[failed]"}
    }
  } atLeft { Log error _.detail }
}
