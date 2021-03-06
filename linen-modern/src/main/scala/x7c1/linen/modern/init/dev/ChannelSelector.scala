package x7c1.linen.modern.init.dev

import android.app.AlertDialog
import android.content.DialogInterface.{OnClickListener, OnMultiChoiceClickListener}
import android.content.{Context, DialogInterface}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.channel.dev.ChannelNameAccessor
import x7c1.wheat.macros.logger.Log

class ChannelSelector private (
  context: Context,
  helper: DatabaseHelper,
  onSelect: OnSelectChannels){

  private val items = ChannelNameAccessor(helper.getReadableDatabase).allNames

  private val selectedItems = collection.mutable.ListMap[Int, Boolean]()

  private val dialog = new AlertDialog.Builder(context).
    setTitle("Channels to add sources").
    setMultiChoiceItems(
      items.map(_.name).toArray[CharSequence],
      null,
      new OnMultiChoiceClickListener {
        override def onClick(dialog: DialogInterface, which: Int, isChecked: Boolean) = {
          if (isChecked) {
            selectedItems(which) = true
          } else {
            selectedItems.remove(which)
          }
        }
      }
    ).
    setPositiveButton("OK", new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        onSelect onSelectPositive ChannelSelectedEvent(selectedChannelNames)
      }
    }).
    setNegativeButton("Cancel", new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        Log info s"canceled"
      }
    }).
    create()

  def showDialog() = dialog.show()

  def selectedChannelIds: Seq[Long] = {
    selectedItems.keys.map(items).toSeq.map(_.channelId)
  }
  def selectedChannelNames: Seq[String] = {
    selectedItems.keys.map(items).toSeq.map(_.name)
  }
}

object ChannelSelector {
  def apply
    (context: Context, helper: DatabaseHelper)
    (f: ChannelSelectedEvent => Unit): ChannelSelector = {

    new ChannelSelector(context, helper, new OnSelectChannels {
      override def onSelectPositive(e: ChannelSelectedEvent): Unit = f(e)
    })
  }
}

trait OnSelectChannels {
  def onSelectPositive(e: ChannelSelectedEvent)
}

case class ChannelSelectedEvent(
  channelTitles: Seq[String]
)
