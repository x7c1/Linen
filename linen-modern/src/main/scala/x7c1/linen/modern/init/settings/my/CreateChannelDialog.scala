package x7c1.linen.modern.init.settings.my

import android.content.DialogInterface.OnClickListener
import android.content.{Context, DialogInterface}
import android.os.Bundle
import android.support.v7.app.{AlertDialog, AppCompatDialogFragment}
import android.view.inputmethod.InputMethodManager
import x7c1.linen.glue.res.layout.SettingMyChannelCreate
import x7c1.linen.modern.init.settings.my.CreateChannelDialog.Arguments
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


object CreateChannelDialog {
  class Arguments(
    val accountId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val inputLayoutFactory: ViewHolderProviderFactory[SettingMyChannelCreate]
  )
}

class CreateChannelDialog extends AppCompatDialogFragment with TypedFragment[Arguments]{
  lazy val args = getTypedArguments

  override def onCreateDialog(savedInstanceState: Bundle) = internalDialog

  override def onStart(): Unit = {
    super.onStart()

    getDialog match {
      case dialog: AlertDialog =>
        dialog.positiveButton foreach (_ onClick { _ =>
          createChannel()
          hideKeyboard()
        })
        dialog.negativeButton foreach (_ onClick { _ =>
          hideKeyboard()
        })
      case dialog => Log error s"unknown dialog $dialog"
    }
  }
  private def createChannel() = {
    Log info s"[create] account:${args.accountId}"
  }
  private def hideKeyboard() = {
    Option(getActivity.getCurrentFocus) match {
      case Some(view) =>
        Log info s"[focus] $view"

        val manager = getActivity.
          getSystemService(Context.INPUT_METHOD_SERVICE).
          asInstanceOf[InputMethodManager]

        Log info s"[focus-token] ${view.getWindowToken}"
        manager.hideSoftInputFromWindow(
          layout.channelName.getWindowToken,
          InputMethodManager.HIDE_NOT_ALWAYS
        )
        val shown = manager.isAcceptingText
        Log info s"focus? $shown"

        val msec = if (shown) 300 else 200
        view.runAfter(msec){ _ => dismiss() }

      case None =>
        Log warn s"[unfocused]"
        dismiss()
    }
  }

  private lazy val layout = {
    val factory = args.inputLayoutFactory create getActivity
    factory.inflateOn(null)
  }

  private lazy val internalDialog = {
    val nop = new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        Log info s"[init]"
      }
    }
    val builder = args.dialogFactory.newInstance(getActivity).
      setTitle("Create new channel").
      setPositiveButton("Create", nop).
      setNegativeButton("Cancel", nop)

    builder setView layout.itemView
    builder.create()
  }
}
