package x7c1.linen.modern.init.inspector

import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AlertDialog
import x7c1.linen.glue.res.layout.SourceSearchStart
import x7c1.linen.modern.init.inspector.StartSearchDialog.Arguments
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log

object StartSearchDialog {

  class Arguments(
    val clientAccountId: Long,
    val dialogFactory: ContextualFactory[AlertDialog.Builder],
    val inputLayoutFactory: ViewHolderProviderFactory[SourceSearchStart]
  )

}

class StartSearchDialog extends DialogFragment with TypedFragment[Arguments] {
  private lazy val args = getTypedArguments

  def showIn(activity: FragmentActivity) = {
    show(activity.getSupportFragmentManager, "start-search-dialog")
  }

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    internalDialog
  }
  private lazy val layout = {
    val factory = args.inputLayoutFactory create getActivity
    factory inflateOn null// todo: eradicate
  }
  private lazy val internalDialog = {
    val nop = new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        Log info s"[init]"
      }
    }
    val builder = args.dialogFactory.newInstance(getActivity).
        setTitle("Search Source").
        setPositiveButton("Start", nop).
        setNegativeButton("Cancel", nop)

    builder setView layout.itemView
    builder.create()
  }
}
