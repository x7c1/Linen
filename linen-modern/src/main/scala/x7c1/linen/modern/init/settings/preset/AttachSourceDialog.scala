package x7c1.linen.modern.init.settings.preset

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
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
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.CheckedState
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.tasks.UiThread

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

class AttachSourceDialog extends DialogFragment with TypedFragment[Arguments]{

  lazy val args = getTypedArguments

  private lazy val helper = new DatabaseHelper(getActivity)

  private lazy val layout = {
    val factory = args.attachLayoutFactory create getActivity
    factory.inflateOn(null)
  }
  private lazy val selectedChannelMap: mutable.Map[Long, Boolean] = {
    mutable.Map()
  }
  private lazy val channelsAccessor = createAccessor() match {
    case Right(accessor) => Some(accessor)
    case Left(e) =>
      Log error format(e){"[failed]"}
      None
  }
  private lazy val internalDialog = {
    val nop = new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        Log info s"[init]"
      }
    }

    /*
      In order to control timing of dismiss(),
        temporally set listeners as nop
        then set onClickListener again in onStart method.
     */
    args.dialogFactory.newInstance(getActivity).
      setTitle("Attached channels").
      setPositiveButton("OK", nop).
      setNegativeButton("CANCEL", nop).
      setView(layout.itemView).
      create()
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
    internalDialog
  }

  override def onStart(): Unit = {
    super.onStart()

    getDialog match {
      case dialog: AlertDialog =>
        dialog.positiveButton foreach (_ onClick onClickPositive)
        dialog.negativeButton foreach (_ onClick onClickNegative)
      case dialog =>
        Log error s"unknown dialog: $dialog"
    }
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
    UiThread.runDelayed(msec = 200){ dismiss() }
  }
  private def onClickNegative(button: Button) = {
    UiThread.runDelayed(msec = 200){ dismiss() }
  }
}

class AttachChannelsAdapter(
  delegatee: AdapterDelegatee[SettingSourceAttachRow, ChannelToAttach],
  state: CheckedState[Long]
) extends BaseAdapter(delegatee){

  override def onBindViewHolder(holder: SettingSourceAttachRow, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (row: SettingSourceAttachRowItem, channel) =>
        row.itemView onClick { _ => row.checked.toggle() }
        row.label.text = channel.channelName
        row.checked bindTo state(channel.channelId, channel.isAttached)
    }

  }
}
