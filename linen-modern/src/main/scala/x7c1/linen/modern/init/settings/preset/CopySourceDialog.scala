package x7c1.linen.modern.init.settings.preset

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AlertDialog
import android.widget.Button
import x7c1.linen.glue.res.layout.SettingChannelSourceCopy
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.linen.modern.init.settings.preset.CopySourceDialog.Arguments
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


object CopySourceDialog {
  class Arguments(
    val clientAccountId: Long,
    val originalSourceId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val copyLayoutFactory: ViewHolderProviderFactory[SettingChannelSourceCopy]
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

  private def onClickPositive(button: Button) = {
    Log info s"[init]"
    dismiss()
  }
  private def onClickNegative(button: Button) = {
    Log info s"[init]"
    dismiss()
  }
}
