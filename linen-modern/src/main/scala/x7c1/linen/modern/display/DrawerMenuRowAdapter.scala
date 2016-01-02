package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel, MenuRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider

class DrawerMenuRowAdapter(
  menuLabelProvider: ViewHolderProvider[MenuRowLabel],
  menuItemProvider: ViewHolderProvider[MenuRowItem] ) extends Adapter[MenuRow]{

  private lazy val providers = new ViewHolderProviders[MenuRow](
    menuLabelProvider,
    menuItemProvider
  )

  override def getItemCount: Int = {
    50
  }

  override def onBindViewHolder(row: MenuRow, position: Int): Unit = {
    row match {
      case holder: MenuRowItem =>
        holder.text setText s"item $position"
      case holder: MenuRowLabel =>
        holder.text setText s"label $position"
    }
  }

  override def getItemViewType(position: Int): Int = {
    if (position % 2 == 0){
      menuLabelProvider.layoutId
    } else {
      menuItemProvider.layoutId
    }
  }

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuRow = {
    val provider = providers find viewType
    provider inflateOn parent
  }
}

private class ViewHolderProviders[A](providers: ViewHolderProvider[_ <: A]*){
  def find(viewType: Int): ViewHolderProvider[_ <: A] =
    providers find (_.layoutId == viewType) getOrElse {
      throw new IllegalArgumentException(s"unknown viewType: $viewType")
    }
}
