package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel, MenuRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider

class DrawerMenuRowAdapter(
  menuLabelProvider: ViewHolderProvider[MenuRowLabel],
  menuItemProvider: ViewHolderProvider[MenuRowItem] ) extends Adapter[MenuRow]{

  override def getItemCount: Int = {
    10
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
      111
    } else {
      222
    }
  }

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuRow = {
    val provider = viewType match {
      case 111 => menuLabelProvider
      case 222 => menuItemProvider
    }
    provider inflateOn parent
  }
}
