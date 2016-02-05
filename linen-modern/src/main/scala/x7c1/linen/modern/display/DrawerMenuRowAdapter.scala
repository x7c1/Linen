package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{MenuRowSeparator, MenuRow, MenuRowItem, MenuRowLabel}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log

class DrawerMenuRowAdapter(
  menuLabelProvider: ViewHolderProvider[MenuRowLabel],
  menuItemProvider: ViewHolderProvider[MenuRowItem],
  menuSeparatorProvider: ViewHolderProvider[MenuRowSeparator],
  boxes: MenuItemsBoxes) extends Adapter[MenuRow]{

  private lazy val providers = new ViewHolderProviders[MenuRow](
    menuLabelProvider,
    menuItemProvider,
    menuSeparatorProvider
  )
  override def getItemCount: Int = boxes.count

  override def onBindViewHolder(row: MenuRow, position: Int): Unit = {
    row match {
      case holder: MenuRowItem =>
        boxes findItemAt position foreach { case (x: DrawerMenuItem) =>
          holder.text setText x.body
          holder.itemView setOnClickListener x.onClick
        }
      case holder: MenuRowLabel =>
        boxes findItemAt position foreach { case (x: DrawerMenuLabel) =>
          holder.text setText x.body
        }
      case holder: MenuRowSeparator =>
      case holder =>
        Log error s"unknown row: $holder"
    }
  }

  override def getItemViewType(position: Int): Int = {
    boxes findItemAt position map (_.viewType) getOrElse {
      throw new IllegalArgumentException(s"invalid position: $position")
    }
  }

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuRow = {
    providers get viewType inflateOn parent
  }
}

private class ViewHolderProviders[A](providers: ViewHolderProvider[_ <: A]*){
  def get(viewType: Int): ViewHolderProvider[_ <: A] =
    providers find (_.layoutId == viewType) getOrElse {
      throw new IllegalArgumentException(s"unknown viewType: $viewType")
    }
}
