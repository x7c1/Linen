package x7c1.linen.modern.init.inspector

import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.widget.Button
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.mixin.ChannelsToAttachRecord
import x7c1.linen.glue.res.layout.{SubscribeSourceLayout, SubscribeSourceRow, SubscribeSourceRowItem}
import x7c1.linen.modern.init.inspector.SubscribeSourceDialog.Arguments
import x7c1.linen.repository.channel.my.{ChannelToAttach, MyChannelConnectionUpdater}
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.lore.dialog.DelayedDialog
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.CheckedState
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

import scala.collection.mutable

object SubscribeSourceDialog {

  class Arguments(
    val clientAccountId: Long,
    val sourceId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val layoutFactory: ViewHolderProviderFactory[SubscribeSourceLayout],
    val rowItemFactory: ViewHolderProviderFactory[SubscribeSourceRowItem]
  )

}

class SubscribeSourceDialog extends DialogFragment
  with DelayedDialog
  with TypedFragment[Arguments] {

  def showIn(activity: FragmentActivity): Unit = {
    show(activity.getSupportFragmentManager, "subscribe-source")
  }

  override def onCreateDialog(savedInstanceState: Bundle) = {
    channelsAccessor foreach { accessor =>
      layout.channels setLayoutManager new LinearLayoutManager(getContext)
      layout.channels setAdapter new AttachChannelsAdapter(
        AdapterDelegatee.create(
          providers = new SubscribeSourceRowProviders(args.rowItemFactory create getContext),
          sequence = accessor
        ),
        CheckedState(selectedChannelMap)
      )
    }
    args.dialogFactory.createAlertDialog(
      title = "Attach to my channels",
      positiveText = "OK",
      negativeText = "CANCEL",
      layoutView = layout.itemView
    )
  }

  override def onStart(): Unit = {
    super.onStart()

    initializeButtons(
      positive = onClickPositive,
      negative = _ => dismissSoon()
    )
  }

  private lazy val args = getTypedArguments

  private lazy val helper = new DatabaseHelper(getActivity)

  private lazy val layout = {
    val factory = args.layoutFactory create getActivity
    factory.inflateOn(null)
  }
  private lazy val selectedChannelMap: mutable.Map[Long, Boolean] = {
    mutable.Map()
  }
  private lazy val channelsAccessor = createAccessor() match {
    case Right(accessor) =>
      Some(accessor)
    case Left(e) =>
      Log error format(e)("[failed]")
      None
  }

  private def onClickPositive(button: Button) = {
    Log info s"[init]"

    channelsAccessor map {
      MyChannelConnectionUpdater(helper, args.sourceId, _)
    } foreach {
      _ updateMapping selectedChannelMap
    }
    dismissSoon()
  }

  private def createAccessor() = {
    helper.selectorOf[ChannelToAttach] traverseOn ChannelsToAttachRecord.Key(
      accountId = args.clientAccountId,
      sourceId = args.sourceId
    )
  }

}

class AttachChannelsAdapter(
  delegatee: AdapterDelegatee[SubscribeSourceRow, ChannelToAttach],
  state: CheckedState[Long]) extends BaseAdapter(delegatee) {

  import x7c1.wheat.modern.decorator.Imports._

  override def onBindViewHolder(holder: SubscribeSourceRow, position: Int) = {
    delegatee.bindViewHolder(holder, position) {
      case (row: SubscribeSourceRowItem, channel) =>
        row.itemView onClick { _ => row.checkbox.toggle() }
        row.label.text = channel.channelName
        row.checkbox bindTo state(channel.channelId, channel.isAttached)
    }
  }
}
