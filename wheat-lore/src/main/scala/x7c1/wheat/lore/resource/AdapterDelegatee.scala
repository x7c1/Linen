package x7c1.wheat.lore.resource

import android.database.StaleDataException
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.ViewGroup
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.resource.ViewHolderProviders
import x7c1.wheat.modern.sequence.Sequence

import scala.language.{higherKinds, reflectiveCalls}

trait AdapterDelegatee[VH <: ViewHolder, A]{

  def count: Int

  def viewTypeAt(position: Int): Int

  def createViewHolder(parent: ViewGroup, viewType: Int): VH

  def bindViewHolder
    (holder: VH, position: Int)
    (block: PartialFunction[(VH, A), Unit]): Unit
}

object AdapterDelegatee {
  def create[
    A, VH <: ViewHolder,
    S <: Sequence[A],
    P <: ViewHolderProviders[VH] : ({type L[X] = ProviderSelectable[S, X]})#L
  ](providers: P, sequence: S): AdapterDelegatee[VH, A] = {

    new AdapterDelegateeImpl(providers, sequence)
  }

  abstract class BaseAdapter[VH <: ViewHolder, A]
    (delegatee: AdapterDelegatee[VH, A]) extends RecyclerView.Adapter[VH]{

    override def getItemCount = {
      delegatee.count
    }
    override def getItemViewType(position: Int) = {
      delegatee viewTypeAt position
    }
    override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
      delegatee.createViewHolder(parent, viewType)
    }
  }
}

private class AdapterDelegateeImpl[
    A, VH <: ViewHolder,
    S <: Sequence[A],
    P <: ViewHolderProviders[VH] : ({type L[X] = ProviderSelectable[S, X]})#L
  ](providers: P, sequence: S) extends AdapterDelegatee[VH, A]{

  override def count = sequence.length

  override def viewTypeAt(position: Int) = {
    val selector = implicitly[ProviderSelectable[S, P]]
    val provider = selector.selectProvider(position, sequence, providers)
    provider.layoutId()
  }
  override def createViewHolder(parent: ViewGroup, viewType: Int) = {
    providers.createViewHolder(parent, viewType)
  }
  override def bindViewHolder
    (holder: VH, position: Int)
    (block: PartialFunction[(VH, A), Unit]) = {

    def attempt() = try sequence findAt position catch {
      case e: StaleDataException =>
        Log warn format(e){"[failed] recoverable"}
        retry()
      case e: Exception =>
        Log error format(e){"[failed] unexpected"}
        None
    }
    def retry() = try sequence findAt position catch {
      case e: Exception =>
        Log error format(e){"[failed] after retried"}
        None
    }
    attempt() -> holder match {
      case (Some(item), _) if block isDefinedAt holder -> item =>
        block(holder -> item)
      case (item, _) =>
        Log error s"unknown item:$item, holder:$holder"
    }
  }
}
