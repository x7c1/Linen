package x7c1.wheat.lore.resource

import android.support.v7.widget.RecyclerView.ViewHolder
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.resource.ViewHolderProviders


trait ProviderSelectable[B <: ViewHolderProviders[_ <: ViewHolder]] {
  def selectProvider(position: Int, last: Int, providers: B): ViewHolderProvider[_]
}

class FooterSelectable[A <: ViewHolderProviders[_ <: ViewHolder]](
  footer: A => ViewHolderProvider[_],
  other: A => ViewHolderProvider[_]) extends ProviderSelectable[A]{

  override def selectProvider(position: Int, last: Int, providers: A) = {
    if (position == last){
      footer(providers)
    } else {
      other(providers)
    }
  }
}
