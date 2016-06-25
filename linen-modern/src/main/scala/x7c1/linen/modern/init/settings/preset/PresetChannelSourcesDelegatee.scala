package x7c1.linen.modern.init.settings.preset

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasChannelStatusKey
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingChannelSourcesLayout, SettingChannelSourcesRow, SettingSourceAttach, SettingSourceAttachRowItem}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.display.settings.{ChannelSourcesSelected, SourceRowAdapter}
import x7c1.linen.modern.init.settings.source.OnSourceMenuSelected
import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.linen.repository.source.setting.SettingSource
import x7c1.linen.scene.source.rating.OnSourceRatingChanged
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.{ViewHolderProvider, ViewHolderProviderFactory}
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.{ClosableSequenceLoader, RecyclerViewReloader}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.resource.MetricsConverter

class PresetChannelSourcesDelegatee (
  activity: FragmentActivity with ActivityControl with ServiceControl,
  layout: SettingChannelSourcesLayout,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  attachLayoutFactory: ViewHolderProviderFactory[SettingSourceAttach],
  attachRowFactory: ViewHolderProviderFactory[SettingSourceAttachRowItem],
  sourceRowProvider: ViewHolderProvider[SettingChannelSourcesRow] ){

  private lazy val helper = new DatabaseHelper(activity)

  private lazy val database = helper.getReadableDatabase

  def setup(): Unit = {
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    val manager = new LinearLayoutManager(activity)
    layout.sourceList setLayoutManager manager

    IntentExpander executeBy activity.getIntent
  }
  def close(): Unit = {
    Log info "[init]"
    database.close()
    helper.close()
  }
  def showSources(event: ChannelSourcesSelected): Unit = {
    setAdapter(event)
    reloader.redrawBy(event).run(CrawlerContext){
      case Right(_) => //nop
      case Left(e) => Log error e.detail
    }
    layout.toolbar setTitle event.channelName
  }
  private def setAdapter[A: HasChannelStatusKey](event: A) = {
    val onRatingChanged = new OnSourceRatingChanged(
      helper = helper,
      reloader = reloader,
      key = event
    )
    layout.sourceList setAdapter new SourceRowAdapter(
      sources = reloader.sequence,
      channelId = implicitly[HasChannelStatusKey[A]].toId(event).channelId,
      viewHolderProvider = sourceRowProvider,
      onMenuSelected = {
        val listener = new OnSourceMenuSelected(
          activity = activity,
          dialogFactory = dialogFactory,
          attachLayoutFactory = attachLayoutFactory,
          attachRowFactory = attachRowFactory
        )
        listener.showMenu
      },
      onRatingChanged = onRatingChanged,
      metricsConverter = MetricsConverter(activity)
    )
  }
  private lazy val reloader = {
    val loader = ClosableSequenceLoader[CrawlerContext, HasChannelStatusKey, SettingSource](database)
    new RecyclerViewReloader(loader, layout.sourceList)
  }
}
