package x7c1.linen.modern.init.settings.order

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingChannelOrderLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.channel.order.ChannelOrderUpdater
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.linen.scene.updater.ChannelNormalizerService
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports.toRichToolbar
import x7c1.wheat.modern.observer.recycler.order.{DraggableSequenceRoute, OnDragListenerToReload, OnDragListenerToSave, SequenceReloader}

class ChannelOrderDelegatee (
  activity: Activity with ActivityControl with ServiceControl,
  layout: SettingChannelOrderLayout,
  providers: ChannelOrderRowProviders ){

  private var targetAccountId: Option[Long] = None

  def onCreate(): Unit = {
    Log info s"[init]"
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    val listener = new OnDragListenerToStyle append
      OnDragListenerToSave(updater) append
      OnDragListenerToReload(CrawlerContext, reloader) append
      new OnDragListenerToNotify(activity)

    val touchHelper = new ItemTouchHelper(route createCallback listener)
    layout.channelList setLayoutManager new LinearLayoutManager(activity)
    layout.channelList setAdapter new ChannelOrderRowAdapter(
      delegatee = AdapterDelegatee.create(providers, route.loader.sequence),
      onDragStart = holder => touchHelper startDrag holder
    )
    touchHelper attachToRecyclerView layout.channelList
    layout.channelList addItemDecoration touchHelper

    IntentExpander executeBy activity.getIntent
  }
  def onDestroy(): Unit = {
    Log info s"[init]"

    targetAccountId match {
      case Some(id) =>
        ChannelNormalizerService(activity) normalizeRanks id
      case None =>
        Log info "channel not reordered"
    }
    helper.close()
  }
  def showChannels(accountId: Long): Unit = {
    updater.updateDefaultRanks(accountId)
    reloader.reload(accountId) run CrawlerContext atLeft {
      Log error _.detail
    }
    targetAccountId = Some(accountId)
  }
  private lazy val helper = {
    new DatabaseHelper(activity)
  }
  private lazy val reloader = {
    SequenceReloader.on[CrawlerContext].create(route.loader, layout.channelList)
  }
  private lazy val route = {
    DraggableSequenceRoute[CrawlerContext, HasAccountId, SubscribedChannel](
      db = helper.getReadableDatabase
    )
  }
  private lazy val updater = {
    ChannelOrderUpdater[SubscribedChannel](helper.getReadableDatabase)
  }
}
