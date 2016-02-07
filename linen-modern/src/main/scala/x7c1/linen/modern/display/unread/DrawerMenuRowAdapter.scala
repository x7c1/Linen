package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel, MenuRowSeparator, MenuRowTitle}
import x7c1.wheat.macros.logger.Log

class DrawerMenuRowAdapter(
  boxes: MenuItemsBoxes[MenuRow]) extends Adapter[MenuRow]{

  override def getItemCount: Int = boxes.count

  override def onBindViewHolder(row: MenuRow, position: Int): Unit = {
    row match {
      case holder: MenuRowLabel =>
        boxes findItemAt position foreach { case (x: MenuLabel) =>
          holder.text setText x.body
          holder.itemView setOnClickListener x.onClick
        }
      case holder: MenuRowTitle =>
        boxes findItemAt position foreach { case (x: MenuTitle) =>
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
    boxes.inflateOn(parent, viewType)
  }
}
