package x7c1.linen.modern.init.unread

import android.content.Context
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.res.layout.UnreadItemsLayout
import x7c1.linen.modern.display.settings.MyChannelSubscriptionChanged
import x7c1.linen.modern.init.settings.my.ChannelCreated
import x7c1.linen.modern.init.settings.preset.PresetChannelSubscriptionChanged
import x7c1.linen.repository.channel.unread.UnreadChannel
import x7c1.linen.repository.channel.unread.selector.UnreadChannelSelector.UnreadChannelLoader
import x7c1.linen.scene.channel.menu.MyChannelDeleted
import x7c1.wheat.macros.intent.LocalBroadcastListener
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{Done, LoaderEvent, SqlError}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.formatter.ThrowableFormatter.format


class OnChannelSubscriptionChanged (
  layout: UnreadItemsLayout,
  loader: UnreadChannelLoader){

  private lazy val listeners = Seq(
    onCreateMyChannel,
    onDeleteMyChannel,
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
    LocalBroadcastListener(update[ChannelCreated])

  private lazy val onDeleteMyChannel =
    LocalBroadcastListener(update[MyChannelDeleted])

  private lazy val onSubscribeMyChannel =
    LocalBroadcastListener(update[MyChannelSubscriptionChanged])

  private lazy val onSubscribePresetChannel =
    LocalBroadcastListener(update[PresetChannelSubscriptionChanged])

  private def update[A: HasAccountId](account: A) = {
    val task = loader.startLoading(account) flatMap notifyAdapter
    task.execute()
  }
  def notifyAdapter(event: LoaderEvent[UnreadChannel]) = CallbackTask[Done[UnreadChannel]]{ f =>
    event match {
      case e: Done[UnreadChannel] =>
        Log info s"[done] ${e.sequence.length}"
        layout.menuList runUi { _.getAdapter.notifyDataSetChanged() }
        f(e)
      case error: SqlError[UnreadChannel] =>
        Log error format(error.cause.getCause){"[failed]"}
    }
  }
}
