package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel, MenuRowSeparator, MenuRowTitle}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.menu.{MenuItems, MenuText}

class DrawerMenuRowAdapter(
  items: MenuItems[MenuRow]) extends Adapter[MenuRow]{

  override def getItemCount: Int = items.length

  override def onBindViewHolder(row: MenuRow, position: Int): Unit = {
    row match {
      case holder: MenuRowLabel =>
        items findItemAt position foreach { case (x: DrawerMenuLabel) =>
          holder.text setText x.text
          holder.itemView setOnClickListener x.onClick
        }
      case holder: MenuRowTitle =>
        items findItemAt position foreach { case (x: MenuText) =>
          holder.text setText x.text
        }
      case holder: MenuRowSeparator =>
      case holder =>
        Log error s"unknown row: $holder"
    }
  }

  override def getItemViewType(position: Int): Int = {
    items findItemAt position map (_.viewType) getOrElse {
      throw new IllegalArgumentException(s"invalid position: $position")
    }
  }

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuRow = {
    items.inflateOn(parent, viewType)
  }
}
