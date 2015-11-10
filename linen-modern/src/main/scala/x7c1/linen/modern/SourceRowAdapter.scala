package x7c1.linen.modern

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.SourceRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._

class SourceRowAdapter(
  provider: ViewHolderProvider[SourceRow]) extends Adapter[SourceRow]{

  val sources = createDummyList

  private def createDummyList = (1 to 100) map { n =>
    Source(
      title = s"sample-title-$n",
      description = s"sample-description-$n" )
  }

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }

  override def onBindViewHolder(holder: SourceRow, position: Int) = {
    val source = sources(position)
    holder.title.text = source.title
    holder.description.text = source.description
  }

  override def getItemCount = sources.length
}

case class Source(
  title: String,
  description: String
)
