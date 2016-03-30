package x7c1.linen.modern.display.settings

import android.support.v7.widget.RecyclerView.Adapter
import android.view.{View, ViewGroup}
import android.widget.{RelativeLayout, SeekBar}
import android.widget.SeekBar.OnSeekBarChangeListener
import x7c1.linen.glue.res.layout.SettingChannelSourcesRow
import x7c1.linen.modern.accessor.SettingSourceAccessor
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class SourceRowAdapter (
  accessor: SettingSourceAccessor,
  viewHolderProvider: ViewHolderProvider[SettingChannelSourcesRow],
  onSyncClicked: OnSyncClickedListener )
  extends Adapter[SettingChannelSourcesRow]{

  override def getItemCount: Int = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingChannelSourcesRow, position: Int): Unit = {
    accessor findAt position foreach { source =>
      holder.title.text = source.title
      holder.description.text = source.description
      holder.switchSubscribe setChecked true
      holder.sync onClick { view =>
        onSyncClicked onSyncClicked SyncClickedEvent(source.sourceId)
      }
      holder.ratingBar setProgress source.rating
      holder.ratingBar setOnSeekBarChangeListener new OnRatingChanged(holder)
      holder.ratingValue.text = s"${source.rating}"
    }
  }
}

private class OnRatingChanged(holder: SettingChannelSourcesRow) extends OnSeekBarChangeListener {
  lazy val original = getOriginal

  var started = false

  override def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean): Unit = {
    if (started){
      val len = seekBar.getThumb.getBounds.left
      holder.ratingValue.text = s"$progress"
//      holder.ratingValue.setX(original + len + (holder.ratingValue.getWidth / 2))

//      val x = original + len + (holder.ratingValue.getWidth / 2)
      val x = original + len
      holder.ratingValue setX x
    }
  }
  override def onStopTrackingTouch(seekBar: SeekBar): Unit = {
    holder.ratingValue setVisibility View.GONE
  }
  override def onStartTrackingTouch(seekBar: SeekBar): Unit = {
    started = true
    holder.ratingValue setVisibility View.VISIBLE
    holder.ratingValue setX original
  }
  def getOriginal = {
    val parentLeft = {
      val a = new Array[Int](2)
      holder.ratingBar.getParent.asInstanceOf[View].getLocationInWindow(a)
      a(0)
    }
    val thisLeft = {
      val a = new Array[Int](2)
      holder.ratingBar.getLocationInWindow(a)
      a(0)
    }
    val params = holder.ratingBar.getLayoutParams.asInstanceOf[RelativeLayout.LayoutParams]
    Log info s"${params.leftMargin}, $params"
    thisLeft - parentLeft + params.leftMargin
  }
}

trait OnSyncClickedListener {
  def onSyncClicked(event: SyncClickedEvent): Unit
}
object OnSyncClickedListener {
  def apply(f: SyncClickedEvent => Unit): OnSyncClickedListener =
    new OnSyncClickedListener {
      override def onSyncClicked(event: SyncClickedEvent): Unit = f(event)
    }
}

case class SyncClickedEvent(
  sourceId: Long
)
