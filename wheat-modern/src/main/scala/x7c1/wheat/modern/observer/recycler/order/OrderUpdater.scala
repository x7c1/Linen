package x7c1.wheat.modern.observer.recycler.order

import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.CanIdentify
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{Done, SqlError}
import x7c1.wheat.modern.decorator.Imports.toRichView
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.observer.recycler.order.DraggableSequenceRoute.{DragFinished, DragStarted, OnDragListener}

import scala.language.{higherKinds, reflectiveCalls}

trait OrderUpdater[A]{
  def update(items: PositionedItems[A]): Unit
}

case class PositionedItems[A](
  current: A,
  previous: Option[A],
  next: Option[A]
)

class OnDragListenerToSave[A] private (
  updater: OrderUpdater[A]) extends OnDragListener[A]{

  override def onStartDragging(event: DragStarted[A]) = {
    // nop
  }
  override def onFinishDragging(event: DragFinished[A]) = {
    val current = event.holder.getAdapterPosition
    event.sequence findAt current map { x =>
      new PositionedItems(
        current = x,
        previous = event.sequence.findAt(current - 1),
        next = event.sequence.findAt(current + 1)
      )
    } match {
      case Some(items) => updater update items
      case None => Log error s"[failed] item not found at:$current"
    }
  }
}

object OnDragListenerToSave {
  def apply[A](
    updater: OrderUpdater[A]): OnDragListenerToSave[A] = {
    new OnDragListenerToSave(updater)
  }
}

class OnDragListenerToReload[I[T] <: CanIdentify[T], A: I] private (
  reloader: SequenceReloader[I, A] ) extends OnDragListener[A]{

  override def onStartDragging(event: DragStarted[A]): Unit = {
    // nop
  }
  override def onFinishDragging(event: DragFinished[A]): Unit = {
    event.sequence findAt 0 match {
      case Some(item) => reloader reload item
      case None => Log error s"[failed] empty sequence"
    }
  }
}

object OnDragListenerToReload {
  def apply[I[T] <: CanIdentify[T], A: I](
    reloader: SequenceReloader[I, A]): OnDragListenerToReload[I, A] = {
    new OnDragListenerToReload(reloader)
  }
}

class SequenceReloader[I[T] <: CanIdentify[T], A](
  loader: ClosableSequenceLoader[I, A],
  recyclerView: RecyclerView ){

  def reload[X: I](x: X): Unit = {
    loader.startLoading(x) apply {
      case _: Done[A] =>
        recyclerView runUi {_.getAdapter.notifyDataSetChanged()}
      case SqlError(e) =>
        Log error format(e.getCause){"[failed]"}
    }
  }
}
