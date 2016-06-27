package x7c1.linen.modern.init.unread

import android.content.Context
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.res.layout.UnreadItemsLayout
import x7c1.linen.modern.display.settings.MyChannelSubscriptionChanged
import x7c1.linen.modern.init.settings.my.ChannelCreated
import x7c1.linen.modern.init.settings.order.ChannelOrdered
import x7c1.linen.modern.init.settings.preset.PresetChannelSubscriptionChanged
import x7c1.linen.repository.channel.unread.UnreadChannel
import x7c1.linen.repository.channel.unread.selector.UnreadChannelSelector.UnreadChannelLoader
import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.linen.scene.channel.menu.MyChannelDeleted
import x7c1.wheat.macros.intent.LocalBroadcastListener
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{LoadingDone, LoadingError}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.kinds.Fate


class OnChannelStatusChanged (
  layout: UnreadItemsLayout,
  loader: UnreadChannelLoader){

  private lazy val listeners = Seq(
    LocalBroadcastListener(update[ChannelOrdered]),
    LocalBroadcastListener(update[ChannelCreated]),
    LocalBroadcastListener(update[MyChannelDeleted]),
    LocalBroadcastListener(update[MyChannelSubscriptionChanged]),
    LocalBroadcastListener(update[PresetChannelSubscriptionChanged])
  )
  def registerTo(context: Context): Unit = {
    listeners foreach { _ registerTo context }
  }
  def unregisterFrom(context: Context): Unit = {
    listeners foreach { _ unregisterFrom context }
  }
  private def update[A: HasAccountId](account: A) = {
    val task = loader startLoading account flatMap notifyAdapter
    task run CrawlerContext atLeft {
      Log error _.detail
    }
  }
  def notifyAdapter(done: LoadingDone[UnreadChannel]): Fate[CrawlerContext, LoadingError, LoadingDone[UnreadChannel]] =
    Fate { x => g =>
      layout.menuList runUi {_.getAdapter.notifyDataSetChanged()}
      g(Right(done))
    }
}
