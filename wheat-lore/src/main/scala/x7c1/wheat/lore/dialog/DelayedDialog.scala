package x7c1.wheat.lore.dialog

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.tasks.UiThread


trait DelayedDialog extends DialogFragment {

  implicit class RichAlertDialogBuilder(factory: ContextualFactory[AlertDialog.Builder]) {

    def createAlertDialog(
      title: String,
      positiveText: String,
      negativeText: String,
      layoutView: View): AlertDialog = {

      val nop = new OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int): Unit = {
          Log info s"[init]"
        }
      }

      /*
        In order to control timing of dismiss(),
          need to set listeners temporally as nop
          then set onClickListener again in onStart method.
       */

      factory.newInstance(getActivity).
        setTitle(title).
        setPositiveButton(positiveText, nop).
        setNegativeButton(negativeText, nop).
        setView(layoutView).
        create()
    }
  }

  protected def dismissLater(): Unit = {
    UiThread.runDelayed(msec = 200){ dismiss() }
  }

  protected def initializeButtons(positive: Button => Unit, negative: Button => Unit): Unit = {
    findInternalDialog foreach { dialog =>
      dialog.positiveButton foreach (_ onClick positive)
      dialog.negativeButton foreach (_ onClick negative)
    }
  }

  private def findInternalDialog: Option[AlertDialog] = {
    getDialog match {
      case dialog: AlertDialog =>
        Some(dialog)
      case dialog =>
        Log error s"unknown dialog: $dialog"
        None
    }
  }

}
