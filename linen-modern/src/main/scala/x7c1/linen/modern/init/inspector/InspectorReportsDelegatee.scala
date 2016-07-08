package x7c1.linen.modern.init.inspector

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.InspectorLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.inspector.InspectorReportRow
import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader

class InspectorReportsDelegatee (
  activity: Activity with ActivityControl with ServiceControl,
  layout: InspectorLayout,
  rowProviders: InspectorRowProviders
){
  def onCreate(): Unit = {

    layout.reports setLayoutManager new LinearLayoutManager(activity)

    IntentExpander executeBy activity.getIntent
  }
  def showInspectorReports(accountId: Long): Unit = {
    layout.reports setAdapter createAdapter(accountId)

    loader.startLoading(accountId).run(CrawlerContext).atLeft {
      Log error _.detail
    }
  }
  def createAdapter[A: HasAccountId](account: A) = {
    new InspectorRowAdapter(
      delegatee = AdapterDelegatee.create(rowProviders, loader.sequence)
    )
  }
  def onDestroy(): Unit = {
    helper.close()
  }
  private lazy val helper = new DatabaseHelper(activity)

  private lazy val loader = {
    ClosableSequenceLoader[
      CrawlerContext,
      HasAccountId,
      InspectorReportRow ](helper.getReadableDatabase)
  }
}
