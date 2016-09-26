package x7c1.linen.modern.init.inspector

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SourceSearchLayout, SourceSearchStart}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.inspector.SourceSearchReportRow
import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.linen.scene.inspector.PageActionStartedEvent
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.fragment.FragmentFactory
import x7c1.wheat.macros.intent.{IntentExpander, LocalBroadcastListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader
import x7c1.wheat.modern.decorator.Imports._

class SourceSearchDelegatee(
  activity: FragmentActivity with ActivityControl with ServiceControl,
  layout: SourceSearchLayout,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  inputLayoutFactory: ViewHolderProviderFactory[SourceSearchStart],
  rowProviders: SearchReportRowProviders
) {

  def onCreate(): Unit = {
    onPageActionStarted registerTo activity

    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    layout.reports setLayoutManager new LinearLayoutManager(activity)

    IntentExpander executeBy activity.getIntent
  }

  def showInspectorReports(accountId: Long): Unit = {
    layout.reports setAdapter createAdapter(accountId)

    layout.buttonToCreate onClick { button =>
      showInputDialog(accountId)
    }
    reloadReports(accountId)
  }

  def createAdapter[A: HasAccountId](account: A) = {
    new SourceSearchRowAdapter(
      delegatee = AdapterDelegatee.create(rowProviders, loader.sequence)
    )
  }

  def onDestroy(): Unit = {
    onPageActionStarted unregisterFrom activity
    helper.close()
  }

  private lazy val helper = new DatabaseHelper(activity)

  private lazy val loader = {
    ClosableSequenceLoader[
      CrawlerContext,
      HasAccountId,
      SourceSearchReportRow](helper.getReadableDatabase)
  }

  private lazy val onPageActionStarted = LocalBroadcastListener {
    reloadReports[PageActionStartedEvent]
  }

  private def showInputDialog(accountId: Long) = {
    val fragment = FragmentFactory.create[StartSearchDialog] by
      new StartSearchDialog.Arguments(
        clientAccountId = accountId,
        dialogFactory = dialogFactory,
        inputLayoutFactory = inputLayoutFactory
      )

    fragment showIn activity
  }

  private def reloadReports[A: HasAccountId](account: A) = {
    val accountId = implicitly[HasAccountId[A]] toId account
    loader.startLoading(accountId).run(CrawlerContext) {
      case Right(_) =>
        layout.reports runUi (_.getAdapter.notifyDataSetChanged())
      case Left(e) =>
        Log error e.detail
    }
  }
}
