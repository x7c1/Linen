package x7c1.linen.modern.init.settings.preset

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import android.widget.{CompoundButton, Button}
import android.widget.CompoundButton.OnCheckedChangeListener
import x7c1.linen.glue.res.layout.{SettingSourceCopy, SettingSourceCopyRow, SettingSourceCopyRowItem}
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.linen.modern.accessor.setting.{CopyTargetChannelsAccessor, MyChannel}
import x7c1.linen.modern.init.settings.preset.CopySourceDialog.Arguments
import x7c1.linen.modern.init.updater.ThrowableFormatter.format
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


object CopySourceDialog {
  class Arguments(
    val clientAccountId: Long,
    val originalChannelId: Long,
    val originalSourceId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val copyLayoutFactory: ViewHolderProviderFactory[SettingSourceCopy],
    val rowFactory: ViewHolderProviderFactory[SettingSourceCopyRowItem]
  )
}

class CopySourceDialog extends DialogFragment with TypedFragment[Arguments]{

  lazy val args = getTypedArguments

  private lazy val helper = new LinenOpenHelper(getActivity)

  private lazy val layout = {
    val factory = args.copyLayoutFactory create getActivity
    factory.inflateOn(null)
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
    val builder = args.dialogFactory.newInstance(getActivity).
      setTitle("Copy source to...").
      setPositiveButton("Copy", nop).
      setNegativeButton("Cancel", nop)

    createAccessor() match {
      case Right(accessor) =>
        layout.channels setLayoutManager new LinearLayoutManager(getContext)
        layout.channels setAdapter new CopyTargetChannelsAdapter(
          AdapterDelegatee.create(
            providers = new SettingSourceCopyRowProviders(args.rowFactory create getContext),
            sequence = accessor
          )
        )
      case Left(e) => Log error format(e){"[failed]"}
    }
    builder setView layout.itemView
    builder.create()
  }
  def showIn(activity: FragmentActivity): Unit = {
    show(activity.getSupportFragmentManager, "create-source")
  }
  override def onCreateDialog(savedInstanceState: Bundle) = internalDialog

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
    CopyTargetChannelsAccessor.create(
      db = helper.getReadableDatabase,
      accountId = args.clientAccountId,
      channelIdToExclude = args.originalChannelId
    )
  }

  private def onClickPositive(button: Button) = {
    Log info s"[init]"
    dismiss()
  }
  private def onClickNegative(button: Button) = {
    Log info s"[init]"
    dismiss()
  }
}

class CopyTargetChannelsAdapter(
  delegatee: AdapterDelegatee[SettingSourceCopyRow, MyChannel]
) extends Adapter[SettingSourceCopyRow] {

  private val checkedMap = collection.mutable.Map[Long, Boolean]()

  override def getItemCount: Int = delegatee.count

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    delegatee.createViewHolder(parent, viewType)
  }
  override def getItemViewType(position: Int) = {
    delegatee viewTypeAt position
  }
  override def onBindViewHolder(holder: SettingSourceCopyRow, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (row: SettingSourceCopyRowItem, channel) =>
        row.itemView onClick { _ => row.checked.toggle() }
        row.label.text = channel.name
        row.checked.setOnCheckedChangeListener(new OnCheckedChangeListener {
          override def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean): Unit = {
            if (isChecked) {
              checkedMap(channel.channelId) = true
            } else {
              checkedMap remove channel.channelId
            }
          }
        })
        row.checked setChecked checkedMap.getOrElse(channel.channelId, false)
    }
  }
}
