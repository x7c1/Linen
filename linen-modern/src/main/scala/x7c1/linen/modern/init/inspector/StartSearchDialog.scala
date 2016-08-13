package x7c1.linen.modern.init.inspector

import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AlertDialog
import android.widget.Button
import x7c1.linen.glue.res.layout.SourceSearchStart
import x7c1.linen.modern.init.inspector.StartSearchDialog.Arguments
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.either.EitherTask
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.dialog.tasks.KeyboardControl


object StartSearchDialog {

  class Arguments(
    val clientAccountId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val inputLayoutFactory: ViewHolderProviderFactory[SourceSearchStart]
  )

}

class StartSearchDialog extends DialogFragment with TypedFragment[Arguments] {
  private lazy val args = getTypedArguments

  private val provide = EitherTask.hold[StartSearchError]

  private lazy val keyboard = {
    KeyboardControl[StartSearchError](this, layout.originUrl)
  }

  def showIn(activity: FragmentActivity) = {
    show(activity.getSupportFragmentManager, "start-search-dialog")
  }

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    internalDialog
  }

  override def onStart(): Unit = {
    super.onStart()

    getDialog match {
      case dialog: AlertDialog =>
        dialog.positiveButton foreach (_ onClick onClickPositive)
        dialog.negativeButton foreach (_ onClick onClickNegative)
    }

  }

  private def onClickPositive(button: Button) = {

  }

  private def onClickNegative(button: Button) = {
    Log info s"[init]"
    keyboard.taskToHide().execute()
  }

  private lazy val layout = {
    val factory = args.inputLayoutFactory create getActivity
    factory inflateOn null // todo: eradicate
  }
  private lazy val internalDialog = {
    val nop = new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        Log info s"[init]"
      }
    }
    val builder = args.dialogFactory.newInstance(getActivity).
      setTitle("Search Sources").
      setPositiveButton("Start", nop).
      setNegativeButton("Cancel", nop)

    builder setView layout.itemView
    builder.create()
  }
}
