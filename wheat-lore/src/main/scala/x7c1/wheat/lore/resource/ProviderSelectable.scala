package x7c1.wheat.lore.resource

import android.support.v7.widget.RecyclerView.ViewHolder
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.resource.ViewHolderProviders
import x7c1.wheat.modern.sequence.Sequence


trait ProviderSelectable[A <: Sequence[_], B <: ViewHolderProviders[_ <: ViewHolder]] {
  def selectProvider(position: Int, sequence: A, providers: B): ViewHolderProvider[_]
}

class WithSingleProvider[B <: ViewHolderProviders[_ <: ViewHolder]](
  target: B => ViewHolderProvider[_]
){
  implicit def selectable[A <: Sequence[_]]: ProviderSelectable[A, B] =
    new ProviderSelectable[A, B] {
      override def selectProvider(position: Int, sequence: A, providers: B) = {
        target(providers)
      }
    }
}

class WithFooter[B <: ViewHolderProviders[_ <: ViewHolder]](
  footer: B => ViewHolderProvider[_],
  other: B => ViewHolderProvider[_]
){
  implicit def selectable[A <: Sequence[_]]: ProviderSelectable[A, B] =
    new ProviderSelectable[A, B] {
      override def selectProvider(position: Int, sequence: A, providers: B) = {
        if (position == sequence.length - 1){
          footer(providers)
        } else {
          other(providers)
        }
      }
    }
}
