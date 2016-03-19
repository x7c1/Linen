package x7c1.wheat.modern.resource

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.ViewGroup
import x7c1.wheat.ancient.resource.ViewHolderProvider

trait ViewHolderProviders [A <: ViewHolder]{

  protected def all: Seq[ViewHolderProvider[_ <: A]]

  def createViewHolder(parent: ViewGroup, viewType: Int): A = {
    val provider = all find (_.layoutId == viewType) getOrElse {
      throw new IllegalArgumentException(s"unknown viewType: $viewType")
    }
    provider inflateOn parent
  }
}
