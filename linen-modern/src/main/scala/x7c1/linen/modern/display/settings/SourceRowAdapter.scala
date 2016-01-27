package x7c1.linen.modern.display.settings

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.SettingChannelSourcesRow
import x7c1.linen.modern.accessor.SettingSourceAccessor
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._

class SourceRowAdapter (
  accessor: SettingSourceAccessor,
  viewHolderProvider: ViewHolderProvider[SettingChannelSourcesRow] )
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
    }
  }

}
