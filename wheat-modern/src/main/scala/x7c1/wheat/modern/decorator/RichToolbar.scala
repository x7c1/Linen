package x7c1.wheat.modern.decorator

import android.support.v7.widget.Toolbar
import android.view.View
import android.view.View.OnClickListener

class RichToolbar[A <: Toolbar](view: A){
  def onClickNavigation[B](f: A => B): Unit = view.setNavigationOnClickListener(
    new OnClickListener {
      override def onClick(v: View): Unit = f(view)
    }
  )
}
