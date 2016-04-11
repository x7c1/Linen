package x7c1.linen.modern.init.unread

import android.content.Context
import x7c1.linen.glue.res.layout.UnreadItemsLayout
import x7c1.linen.modern.accessor.unread.ChannelLoaderEvent.{AccessorError, Done}
import x7c1.linen.modern.accessor.unread.{ChannelLoaderEvent, UnreadChannelLoader}
import x7c1.linen.modern.display.settings.MyChannelSubscriptionChanged
import x7c1.linen.modern.init.settings.my.ChannelCreated
import x7c1.linen.modern.init.settings.preset.PresetChannelSubscriptionChanged
import x7c1.wheat.macros.intent.LocalBroadcastListener
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.decorator.Imports._


class OnChannelSubscriptionChanged (
  layout: UnreadItemsLayout,
  loader: => Option[UnreadChannelLoader]){

  private lazy val listeners = Seq(
    onCreateMyChannel,
    onSubscribeMyChannel,
    onSubscribePresetChannel
  )
  def registerTo(context: Context): Unit = {
    listeners foreach { _ registerTo context }
  }
  def unregisterFrom(context: Context): Unit = {
    listeners foreach { _ unregisterFrom context }
  }
  private lazy val onCreateMyChannel =
    LocalBroadcastListener[ChannelCreated]{ _ => update() }

  private lazy val onSubscribeMyChannel =
    LocalBroadcastListener[MyChannelSubscriptionChanged]{ event => update() }

  private lazy val onSubscribePresetChannel =
    LocalBroadcastListener[PresetChannelSubscriptionChanged]{ event => update() }

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
