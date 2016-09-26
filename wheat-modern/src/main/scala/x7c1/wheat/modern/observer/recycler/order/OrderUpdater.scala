package x7c1.wheat.modern.observer.recycler.order

import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.CanIdentify
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.LoadingError
import x7c1.wheat.modern.decorator.Imports.toRichView
import x7c1.wheat.modern.fate.FateProvider.HasContext
import x7c1.wheat.modern.kinds.Fate
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

class OnDragListenerToReload[C: HasContext, I[T] <: CanIdentify[T], A: I] private (
  context: C,
  reloader: SequenceReloader[C, I, A] ) extends OnDragListener[A]{

  override def onStartDragging(event: DragStarted[A]): Unit = {
    // nop
  }
  override def onFinishDragging(event: DragFinished[A]): Unit = {
    event.sequence findAt 0 match {
      case Some(item) =>
        reloader.reload(item) run context atLeft {
          Log error _.detail
        }
      case None => Log error s"[failed] empty sequence"
    }
  }
}

object OnDragListenerToReload {
  def apply[C: HasContext, I[T] <: CanIdentify[T], A: I](
    context: C,
    reloader: SequenceReloader[C, I, A]): OnDragListenerToReload[C, I, A] = {
    new OnDragListenerToReload(context, reloader)
  }
}

class SequenceReloader[C: HasContext, I[T] <: CanIdentify[T], A] private (
  loader: ClosableSequenceLoader[C, I, A],
  recyclerView: RecyclerView ){

  def reload[X: I](x: X): Fate[C, LoadingError, Unit] = {
    loader.startLoading(x) map { _ =>
      recyclerView runUi {_.getAdapter.notifyDataSetChanged()}
    }
  }
}

object SequenceReloader {
  def on[C: HasContext]: AppliedFactory[C] = new AppliedFactory[C]

  class AppliedFactory[C: HasContext]{
    def create[I[T] <: CanIdentify[T], A](
      loader: ClosableSequenceLoader[C, I, A],
      recyclerView: RecyclerView ): SequenceReloader[C, I, A] = {

      new SequenceReloader(loader, recyclerView)
    }
  }
}
