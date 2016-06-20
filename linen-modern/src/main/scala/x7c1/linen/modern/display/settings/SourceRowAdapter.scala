package x7c1.linen.modern.display.settings

import android.animation.{Animator, AnimatorListenerAdapter}
import android.support.v7.widget.RecyclerView.Adapter
import android.view.{View, ViewGroup}
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.{RelativeLayout, SeekBar}
import x7c1.linen.database.struct.HasSourceStatusKey
import x7c1.linen.glue.res.layout.SettingChannelSourcesRow
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.source.setting.SettingSourceAccessor
import x7c1.linen.scene.source.rating.SourceRatingChanged
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.resource.MetricsConverter

class SourceRowAdapter (
  accessor: SettingSourceAccessor,
  account: ClientAccount,
  channelId: Long,
  viewHolderProvider: ViewHolderProvider[SettingChannelSourcesRow],
  onMenuSelected: SourceMenuSelected => Unit,
  onRatingChanged: SourceRatingChanged => Unit,
  metricsConverter: MetricsConverter ) extends Adapter[SettingChannelSourcesRow]{

  override def getItemCount: Int = accessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewHolderProvider inflateOn parent
  }
  override def onBindViewHolder(holder: SettingChannelSourcesRow, position: Int): Unit = {
    accessor findAt position foreach { source =>
      holder.title.text = source.title
      holder.description toggleVisibility source.description
      holder.menu onClick { view =>
        onMenuSelected apply SourceMenuSelected(
          targetView = view,
          clientAccount = account,
          channelId = channelId,
          source = source
        )
      }
      holder.switchSubscribe setChecked true
      holder.ratingLabel.text = s"RATING:${source.rating}"
      holder.ratingBar setProgress source.rating
      holder.ratingBar setOnSeekBarChangeListener new OnRatingChanged(
        holder,
        sourceStatus = source,
        ratingRadiusPixel = metricsConverter.dipToPixel(16),
        onRatingChanged = onRatingChanged
      )
      holder.ratingValue.text = s"${source.rating}"
      holder.ratingValue setVisibility View.GONE
    }
  }
}

private class OnRatingChanged[A: HasSourceStatusKey](
  holder: SettingChannelSourcesRow,
  sourceStatus: A,
  ratingRadiusPixel: => Int,
  onRatingChanged: SourceRatingChanged => Unit ) extends OnSeekBarChangeListener {

  lazy val initial = getDefault
  var started = false
  val minStep = 5

  override def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean): Unit = {
    if (started){
      val rating = toStepped(progress)
      holder.ratingValue.text = s"$rating"
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
    val rating = toStepped(seekBar.getProgress)
    holder.ratingLabel.text = s"RATING:$rating"

    onRatingChanged apply SourceRatingChanged(
      sourceStatusKey = implicitly[HasSourceStatusKey[A]] toId sourceStatus,
      rating = rating
    )
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
  private def toStepped(progress: Int) = {
    Math.round(progress/ minStep) * minStep
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
