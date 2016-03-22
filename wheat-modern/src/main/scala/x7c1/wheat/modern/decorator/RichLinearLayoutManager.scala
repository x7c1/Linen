package x7c1.wheat.modern.decorator

import android.support.v7.widget.LinearLayoutManager

class RichLinearLayoutManager (manager: LinearLayoutManager){

  def firstCompletelyVisiblePosition: Option[Int] = {
    toOption(manager.findFirstCompletelyVisibleItemPosition())
  }
  def firstVisiblePosition: Option[Int] = {
    toOption(manager.findFirstVisibleItemPosition())
  }
  private def toOption(x: Int) = x match {
    case _ if x < 0 => None
    case _ => Some(x)
  }
}
