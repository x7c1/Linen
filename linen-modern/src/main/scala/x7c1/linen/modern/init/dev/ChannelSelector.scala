package x7c1.linen.modern.init.dev

import android.app.AlertDialog
import android.content.DialogInterface.{OnClickListener, OnMultiChoiceClickListener}
import android.content.{Context, DialogInterface}
import x7c1.wheat.macros.logger.Log

class ChannelSelector private (context: Context, onSelect: OnSelectChannels){

  private val items = (1 to 10).map{ n => s"dummy-channel-$n" }

  private val selectedItems = collection.mutable.ListMap[Int, Boolean]()

  private val dialog = new AlertDialog.Builder(context).
    setTitle("channel to add source").
    setMultiChoiceItems(items.toArray[CharSequence], null, new OnMultiChoiceClickListener {
      override def onClick(dialog: DialogInterface, which: Int, isChecked: Boolean): Unit = {
        Log info s"which:$which"
        if (isChecked)
          selectedItems(which) = true
        else
          selectedItems.remove(which)
      }
    }).
    setPositiveButton("OK", new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        onSelect onSelectPositive ChannelSelectedEvent(selectedTitles)
      }
    }).
    setNegativeButton("Cancel", new OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        Log info s"canceled"
      }
    }).
    create()

  def showDialog() = dialog.show()

  def selectedTitles = selectedItems.keys.map(items).toSeq
}

object ChannelSelector {
  def apply(context: Context)(f: ChannelSelectedEvent => Unit): ChannelSelector = {
    new ChannelSelector(context, new OnSelectChannels {
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
