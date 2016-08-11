package x7c1.linen.modern.init.inspector

import android.support.v4.app.{DialogFragment, FragmentActivity}
import x7c1.linen.modern.init.inspector.StartSearchDialog.Arguments
import x7c1.wheat.macros.fragment.TypedFragment

object StartSearchDialog {

  class Arguments(
    val clientAccountId: Long
  )

}

class StartSearchDialog extends DialogFragment with TypedFragment[Arguments] {

  def showIn(activity: FragmentActivity) = {
    show(activity.getSupportFragmentManager, "start-search-dialog")
  }
}
