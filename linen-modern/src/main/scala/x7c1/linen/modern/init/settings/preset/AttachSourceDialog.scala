package x7c1.linen.modern.init.settings.preset

import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.widget.Button
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.mixin.ChannelsToAttachRecord
import x7c1.linen.glue.res.layout.{SettingSourceAttach, SettingSourceAttachRow, SettingSourceAttachRowItem}
import x7c1.linen.modern.init.settings.preset.AttachSourceDialog.Arguments
import x7c1.linen.repository.channel.my.{ChannelToAttach, MyChannelConnectionUpdater}
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.lore.dialog.DelayedDialog
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.CheckedState
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

import scala.collection.mutable


object AttachSourceDialog {

  class Arguments(
    val clientAccountId: Long,
    val originalChannelId: Long,
    val originalSourceId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val attachLayoutFactory: ViewHolderProviderFactory[SettingSourceAttach],
    val rowFactory: ViewHolderProviderFactory[SettingSourceAttachRowItem]
  )

}

class AttachSourceDialog extends DialogFragment
  with DelayedDialog
  with TypedFragment[Arguments] {

  lazy val args = getTypedArguments

  private lazy val helper = new DatabaseHelper(getActivity)

  private lazy val layout = {
    args.attachLayoutFactory.create(getActivity).inflate()
  }
  private lazy val selectedChannelMap: mutable.Map[Long, Boolean] = {
    mutable.Map()
  }
  private lazy val channelsAccessor = createAccessor() match {
    case Right(accessor) => Some(accessor)
    case Left(e) =>
      Log error format(e) {
        "[failed]"
      }
      None
  }

  def showIn(activity: FragmentActivity): Unit = {
    show(activity.getSupportFragmentManager, "create-source")
  }

  override def onCreateDialog(savedInstanceState: Bundle) = {
    channelsAccessor foreach {
      accessor =>
        layout.channels setLayoutManager new LinearLayoutManager(getContext)
        layout.channels setAdapter new AttachChannelsAdapter(
          AdapterDelegatee.create(
            providers = new SettingSourceAttachRowProviders(args.rowFactory create getContext),
            sequence = accessor
          ),
          CheckedState(selectedChannelMap)
        )
    }
    args.dialogFactory.createAlertDialog(
      title = "Attached channels",
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

  override def onStop(): Unit = {
    super.onStop()
    helper.close()
  }

  private def createAccessor() = {
    helper.selectorOf[ChannelToAttach] traverseOn ChannelsToAttachRecord.Key(
      accountId = args.clientAccountId,
      sourceId = args.originalSourceId
    )
  }

  private def onClickPositive(button: Button) = {
    Log info s"[init]"

    channelsAccessor map {
      MyChannelConnectionUpdater(helper, args.originalSourceId, _)
    } foreach {
      _ updateMapping selectedChannelMap
    }
    dismissSoon()
  }
}

class AttachChannelsAdapter(
  delegatee: AdapterDelegatee[SettingSourceAttachRow, ChannelToAttach],
  state: CheckedState[Long]
) extends BaseAdapter(delegatee) {

  override def onBindViewHolder(holder: SettingSourceAttachRow, position: Int) = {
    delegatee.bindViewHolder(holder, position) {
      case (row: SettingSourceAttachRowItem, channel) =>
        row.itemView onClick { _ => row.checked.toggle() }
        row.label.text = channel.channelName
        row.checked bindTo state(channel.channelId, channel.isAttached)
    }

  }
}
