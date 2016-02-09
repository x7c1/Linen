package x7c1.linen.modern.display.unread

import android.app.Activity
import android.support.v7.widget.RecyclerView.Adapter
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.UnreadDetailRow
import x7c1.linen.modern.accessor.EntryAccessor
import x7c1.linen.modern.struct.EntryDetail
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.action.SiteVisitor
import x7c1.wheat.modern.decorator.Imports._


class DetailRowAdapter(
  entryAccessor: EntryAccessor[EntryDetail],
  selectedListener: OnDetailSelectedListener,
  visitSelectedListener: OnDetailVisitListener,
  viewHolderProvider: ViewHolderProvider[UnreadDetailRow]) extends Adapter[UnreadDetailRow] {

  override def getItemCount: Int = entryAccessor.length

  override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): UnreadDetailRow = {
    viewHolderProvider.inflateOn(viewGroup)
  }
  override def onBindViewHolder(holder: UnreadDetailRow, position: Int): Unit = {
    entryAccessor findAt position foreach { entry =>
      holder.title.text = entry.fullTitle
      holder.content.text = Html.fromHtml(entry.fullContent)
      holder.content setMovementMethod LinkMovementMethod.getInstance()
      holder.createdAt.text = entry.createdAt.format
      holder.itemView onClick { _ =>
        val event = DetailSelectedEvent(position, entry)
        selectedListener onEntryDetailSelected event
      }
      holder.visit onClick { _ =>
        visitSelectedListener onVisit entry
      }
    }
  }

}

trait OnDetailSelectedListener {
  def onEntryDetailSelected(event: DetailSelectedEvent): Unit
}

case class DetailSelectedEvent(position: Int, entry: EntryDetail){
  def dump: String = s"position:$position, entry:$entry"
}

trait OnDetailVisitListener {
  def onVisit(target: EntryDetail): Unit
}
object OnDetailVisitListener {
  def toOpenUrl(activity: Activity): OnDetailVisitListener = {
    new OnVisitImpl(SiteVisitor(activity))
  }
  private class OnVisitImpl(visitor: SiteVisitor) extends OnDetailVisitListener {
    override def onVisit(target: EntryDetail): Unit = {
      Log info target.url
      visitor open target
    }
  }
}
