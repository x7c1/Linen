package x7c1.linen.modern.init.inspector

import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AlertDialog
import android.widget.Button
import x7c1.linen.glue.res.layout.{SubscribeSourceLayout, SubscribeSourceRowItem}
import x7c1.linen.modern.init.inspector.SubscribeSourceDialog.Arguments
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.lore.dialog.DelayedDialog
import x7c1.wheat.macros.fragment.TypedFragment
import x7c1.wheat.macros.logger.Log

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

  private lazy val layout = {
    val factory = args.layoutFactory create getActivity
    factory.inflateOn(null)
  }

  private def onClickPositive(button: Button) = {
    Log info s"[init]"
    dismissSoon()
  }

}
