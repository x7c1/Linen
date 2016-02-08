package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel, MenuRowSeparator, MenuRowTitle}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.menu.{MenuItems, MenuText}

class DrawerMenuRowAdapter(
  items: MenuItems[MenuRow]) extends Adapter[MenuRow]{

  override def getItemCount: Int = items.length

  override def onBindViewHolder(holder: MenuRow, position: Int): Unit = {
    items.bind(holder, position){
      case (row: MenuRowLabel, item: DrawerMenuLabel) =>
        row.text setText item.text
        row.itemView setOnClickListener item.onClick

      case (row: MenuRowTitle, item: MenuText) =>
        row.text setText item.text

      case (row: MenuRowSeparator, _) =>
      case (row, item) =>
        Log error s"unknown row:$row, item:$item, position:$position"
    }
  }

  override def getItemViewType(position: Int): Int = {
    items.viewTypeAt(position)
  }

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuRow = {
    items.inflate(parent, viewType)
  }
}
