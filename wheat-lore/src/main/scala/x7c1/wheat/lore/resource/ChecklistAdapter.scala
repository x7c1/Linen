package x7c1.wheat.lore.resource

import android.support.v7.widget.RecyclerView.ViewHolder
import android.widget.{CheckBox, TextView}
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.lore.resource.ChecklistAdapter.{HasItem, HasRow}
import x7c1.wheat.modern.decorator.CheckedState
import x7c1.wheat.modern.decorator.Imports._

class ChecklistAdapter[ROW <: ViewHolder : HasRow, ITEM, ID] private (
  delegatee: AdapterDelegatee[ROW, ITEM],
  state: CheckedState[ID])(
  implicit itemAccessor: HasItem[ITEM, ID]) extends BaseAdapter(delegatee){

  override def onBindViewHolder(holder: ROW, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (row, item) =>
        val rowAccessor = implicitly[HasRow[ROW]]
        row.itemView onClick { _ =>
          rowAccessor.checkBox(row).toggle()
        }
        rowAccessor.textView(row).text = itemAccessor label item
        rowAccessor.checkBox(row) bindTo state(
          itemAccessor id item,
          itemAccessor defaultChecked item
        )
    }
  }
}

object ChecklistAdapter {

  trait HasRow[A]{
    def textView(row: A): TextView
    def checkBox(row: A): CheckBox
  }

  trait HasItem[A, ID]{
    def id(item: A): ID
    def label(item: A): String
    def defaultChecked(item: A): Boolean
  }

  def apply[ROW <: ViewHolder : HasRow, ITEM, ID](
    delegatee: AdapterDelegatee[ROW, ITEM],
    state: CheckedState[ID])(
    implicit itemAccessor: HasItem[ITEM, ID]): ChecklistAdapter[ROW, ITEM, ID] = {

    new ChecklistAdapter(delegatee, state)
  }
}
