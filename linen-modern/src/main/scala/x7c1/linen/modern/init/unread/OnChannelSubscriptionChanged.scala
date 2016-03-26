package x7c1.linen.modern.init.unread

import android.content.Context
import x7c1.linen.glue.res.layout.UnreadItemsLayout
import x7c1.linen.modern.accessor.unread.ChannelLoaderEvent.{AccessorError, Done}
import x7c1.linen.modern.accessor.unread.{ChannelLoaderEvent, UnreadChannelLoader}
import x7c1.linen.modern.display.settings.MyChannelSubscribeChanged
import x7c1.linen.modern.init.settings.preset.SubscribeChangedEvent
import x7c1.wheat.macros.intent.LocalBroadcastListener
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.decorator.Imports._


class OnChannelSubscriptionChanged (
  layout: UnreadItemsLayout,
  loader: => Option[UnreadChannelLoader]){

  def registerTo(context: Context): Unit = {
    onSubscribeMyChannel registerTo context
    onSubscribePresetChannel registerTo context
  }
  def unregisterFrom(context: Context): Unit = {
    onSubscribeMyChannel unregisterFrom context
    onSubscribePresetChannel unregisterFrom context
  }
  private lazy val onSubscribeMyChannel =
    LocalBroadcastListener[MyChannelSubscribeChanged]{ event => update() }

  private lazy val onSubscribePresetChannel =
    LocalBroadcastListener[SubscribeChangedEvent]{ event => update() }

  private def update() = {
    val task = loader map (_.startLoading() flatMap notifyAdapter)
    task foreach (_.execute())
  }
  def notifyAdapter(event: ChannelLoaderEvent) = CallbackTask[Done]{ f =>
    event match {
      case e: Done =>
        Log info s"[done]"
        layout.menuList runUi { _.getAdapter.notifyDataSetChanged() }
        f(e)
      case e: AccessorError =>
        Log error e.detail
    }
  }
}
