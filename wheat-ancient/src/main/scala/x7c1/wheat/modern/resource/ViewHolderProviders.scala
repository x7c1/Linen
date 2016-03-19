package x7c1.wheat.modern.resource

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.ViewGroup
import x7c1.wheat.ancient.resource.ViewHolderProvider

trait ViewHolderProviders [A <: ViewHolder]{

  protected def all: Seq[ViewHolderProvider[_ <: A]]

  def getWithLayoutParams
    (parent: ViewGroup, viewType: Int)
    (f: PartialFunction[(A, ViewGroup.LayoutParams), Unit]): A = {

    val holder = all find (_.layoutId == viewType) map {
      _ inflateOn parent
    } getOrElse {
      throw new IllegalArgumentException(s"unknown viewType: $viewType")
    }
    val params = holder.itemView.getLayoutParams
    if (f isDefinedAt (holder, params)){
      f(holder, params)
      holder.itemView setLayoutParams params
    }
    holder
  }
}
