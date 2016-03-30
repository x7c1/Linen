package x7c1.linen.modern.display.settings

import android.animation.{Animator, AnimatorListenerAdapter}
import android.support.v7.widget.RecyclerView.Adapter
import android.view.{View, ViewGroup}
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.{RelativeLayout, SeekBar}
import x7c1.linen.glue.res.layout.SettingChannelSourcesRow
import x7c1.linen.modern.accessor.SettingSourceAccessor
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.resource.MetricsConverter

class SourceRowAdapter (
  accessor: SettingSourceAccessor,
  viewHolderProvider: ViewHolderProvider[SettingChannelSourcesRow],
  onSyncClicked: OnSyncClickedListener,
  metricsConverter: MetricsConverter ) extends Adapter[SettingChannelSourcesRow]{

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
      holder.ratingBar setOnSeekBarChangeListener new OnRatingChanged(
        holder,
        ratingRadiusPixel = metricsConverter.dipToPixel(16)
      )
      holder.ratingValue.text = s"${source.rating}"
      holder.ratingValue setVisibility View.GONE
    }
  }
}

private class OnRatingChanged(
  holder: SettingChannelSourcesRow,
  ratingRadiusPixel: => Int ) extends OnSeekBarChangeListener {

  lazy val initial = getDefault
  var started = false

  override def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean): Unit = {
    if (started){
      holder.ratingValue.text = s"$progress"
      holder.ratingValue setX {
        val current = seekBar.getThumb.getBounds.left
        initial + current - (holder.ratingValue.getWidth / 2)
      }
    }
  }
  override def onStopTrackingTouch(seekBar: SeekBar): Unit = {
    holder.ratingValue.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter {
      override def onAnimationEnd(animation: Animator): Unit = {
        holder.ratingValue setVisibility View.GONE
      }
    })
  }
  override def onStartTrackingTouch(seekBar: SeekBar): Unit = {
    started = true

    holder.ratingValue setX initial
    holder.ratingValue setAlpha 0
    holder.ratingValue setVisibility View.VISIBLE
    holder.ratingValue.animate().setDuration(200).alpha(1).setListener(new AnimatorListenerAdapter {
      override def onAnimationEnd(animation: Animator): Unit = {
        holder.ratingValue setVisibility View.VISIBLE
      }
    })
  }
  private def getLeft(view: View) = {
    val x = new Array[Int](2)
    view getLocationInWindow x
    x(0)
  }
  private def getDefault = {
    val parentLeft = getLeft(holder.ratingBar.getParent.asInstanceOf[View])
    val barLeft = getLeft(holder.ratingBar)
    val params = holder.ratingBar.getLayoutParams.asInstanceOf[RelativeLayout.LayoutParams]
    barLeft - parentLeft + params.leftMargin + ratingRadiusPixel
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
