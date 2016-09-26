package x7c1.linen.modern.init.inspector

import java.net.URL

import android.app.Dialog
import android.content.DialogInterface.OnClickListener
import android.content.{Context, DialogInterface}
import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AlertDialog
import android.widget.Button
import x7c1.linen.glue.res.layout.SourceSearchStart
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.init.inspector.StartSearchDialog.Arguments
import x7c1.linen.repository.inspector.ActionPageUrl
import x7c1.linen.repository.inspector.ActionPageUrlError.{EmptyUrl, InvalidFormat}
import x7c1.linen.scene.inspector.InspectorService
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log
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

  private lazy val keyboard = {
    KeyboardControl[StartSearchError](this, layout.originUrl)
  }

  def showIn(activity: FragmentActivity): Unit = {
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
    val context = getActivity.asInstanceOf[Context with ServiceControl]

    ActionPageUrl.create(
      accountId = args.clientAccountId,
      url = layout.originUrl.text.toString
    ) match {
      case Right(pageUrl) =>
        InspectorService(context) inspect pageUrl
        keyboard.taskToHide().execute()

      case Left(e: EmptyUrl) =>
        layout.originUrlLayout setError "(required)"

      case Left(e: InvalidFormat) =>
        layout.originUrlLayout setError {
          e.cause.map(_.getMessage) getOrElse "invalid format"
        }
        Log info e.detail

      case Left(e) =>
        layout.originUrlLayout setError {
          e.cause.map(_.getMessage) getOrElse "unknown format"
        }
        Log error e.detail
    }
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
      setTitle("Search sources").
      setPositiveButton("Start", nop).
      setNegativeButton("Cancel", nop)

    builder setView layout.itemView
    builder.create()
  }
}
