package x7c1.linen.modern.init.unread.entry

import android.support.v7.widget.RecyclerView.ViewHolder
import x7c1.linen.glue.res.layout.{UnreadDetailRow, UnreadDetailRowEntry, UnreadDetailRowFooter, UnreadDetailRowSource, UnreadOutlineRow, UnreadOutlineRowEntry, UnreadOutlineRowFooter, UnreadOutlineRowSource}
import x7c1.linen.repository.entry.unread.{EntryAccessor, UnreadEntry}
import x7c1.linen.repository.unread.{EntryKind, SourceKind}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.lore.resource.ProviderSelectable
import x7c1.wheat.modern.resource.ViewHolderProviders

abstract class EntryRowProviders[A <: ViewHolder] extends ViewHolderProviders[A]{
  def forSource: ViewHolderProvider[_]
  def forEntry: ViewHolderProvider[_]
  def forFooter: ViewHolderProvider[_]
}

object EntryRowProviders {
  implicit def selectable[
    A <: UnreadEntry,
    B <: EntryRowProviders[_ <: ViewHolder]
  ]: ProviderSelectable[EntryAccessor[A], B] =
    new ProviderSelectable[EntryAccessor[A], B] {
      override def selectProvider(position: Int, sequence: EntryAccessor[A], providers: B) = {
        sequence.findKindAt(position) match {
          case Some(SourceKind) => providers.forSource
          case Some(EntryKind) => providers.forEntry
          case _ => providers.forFooter
        }
      }
    }
}

class OutlineListProviders(
  val forSource: ViewHolderProvider[UnreadOutlineRowSource],
  val forEntry: ViewHolderProvider[UnreadOutlineRowEntry],
  val forFooter: ViewHolderProvider[UnreadOutlineRowFooter]
) extends EntryRowProviders[UnreadOutlineRow] {

  override protected val all = Seq(
    forSource,
    forEntry,
    forFooter
  )
}

class DetailListProviders(
  val forSource: ViewHolderProvider[UnreadDetailRowSource],
  val forEntry: ViewHolderProvider[UnreadDetailRowEntry],
  val forFooter: ViewHolderProvider[UnreadDetailRowFooter]
) extends EntryRowProviders[UnreadDetailRow] {

  override protected val all = Seq(
    forSource,
    forEntry,
    forFooter
  )
}
