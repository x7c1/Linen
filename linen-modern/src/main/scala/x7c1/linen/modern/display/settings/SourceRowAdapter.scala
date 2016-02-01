package x7c1.linen.modern.display.settings

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.SettingChannelSourcesRow
import x7c1.linen.modern.accessor.SettingSourceAccessor
import x7c1.wheat.ancient.resource.ViewHolderProvider
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
      holder.ratingBar setProgress source.rating
      holder.sync onClick { view =>
        onSyncClicked onSyncClicked SyncClickedEvent(source.sourceId)
      }
    }
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
