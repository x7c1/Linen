package x7c1.wheat.modern.observer.recycler.order

import java.util
import java.util.Collections

import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper.{ACTION_STATE_DRAG, Callback, DOWN, UP}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.either.EitherTask
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, ClosableSequenceLoader, TraverseOn}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanProvideSelector}
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.observer.recycler.order.DraggableSequenceRoute.{DragFinished, DragStarted, OnDragListener}
import x7c1.wheat.modern.sequence.{CanFilterFrom, Sequence}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.language.higherKinds

class DraggableSequenceRoute[I[T] <: CanIdentify[T], A] private (
  underlying: ClosableSequenceLoader[I, A]){

  val loader: ClosableSequenceLoader[I, A] =
    new ClosableSequenceLoader[I, A] {
      override def sequence = underlying.sequence

      override def closeCursor() = underlying.closeCursor()

      override def startLoading[X: I](x: X) = {
        for {
          event <- underlying startLoading x
          _ <- task { positions = init() }
        } yield {
          event
        }
      }
      override def startLoading2[X: I](x: X)(implicit i: ExecutionContext) = {
        for {
          event <- underlying startLoading2 x
          _ <- EitherTask unit { positions = init() }
        } yield {
          event
        }
      }
    }

  def createCallback(listener: OnDragListener[A]): Callback =
    new Callback {
      override def getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder) = {
        Callback.makeFlag(ACTION_STATE_DRAG, UP | DOWN)
      }
      override def onSwiped(viewHolder: ViewHolder, direction: Int) = {
        // nop
      }
      override def onSelectedChanged(viewHolder: ViewHolder, actionState: Int) = {
        super.onSelectedChanged(viewHolder, actionState)
        try actionState match {
          case ACTION_STATE_DRAG =>
            listener onStartDragging DragStarted(viewHolder, orderedSequence)
          case _ =>
            // nop
        } catch {
          case e: Exception =>
            Log error format(e){"[failed]"}
        }
      }
      override def onMove(recyclerView: RecyclerView, holder: ViewHolder, target: ViewHolder) = {
        val (from, to) = holder.getAdapterPosition -> target.getAdapterPosition
        recyclerView.getAdapter.notifyItemMoved(from, to)

        if (from < to) (from until to) foreach { n =>
          Collections.swap(positions, n, n + 1)
        } else from.until(to, -1) foreach { n =>
          Collections.swap(positions, n, n - 1)
        }
        true
      }
      override def clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) = {
        super.clearView(recyclerView, viewHolder)
        try {
          listener onFinishDragging DragFinished(viewHolder, orderedSequence)
        } catch {
          case e: Exception =>
            Log error format(e){"[failed]"}
        }
      }
    }

  private def orderedSequence: Sequence[A] = {
    val ordered = Sequence from positions.asScala
    implicitly[CanFilterFrom[Sequence]].asFiltered(underlying.sequence)(ordered)
  }
  private var positions = init()

  private def init(): util.List[Int] = {
    util.Arrays.asList(0 until underlying.sequence.length :_*)
  }
}

object DraggableSequenceRoute {
  def apply[I[T] <: CanIdentify[T], A](db: SQLiteDatabase)
    (implicit
      x1: CanTraverse[I, A],
      x2: CanProvideSelector[A]{ type Selector <: TraverseOn[I, A] }
    ): DraggableSequenceRoute[I, A] = {

    new DraggableSequenceRoute[I, A](ClosableSequenceLoader(db))
  }
  case class DragStarted[A](
    holder: ViewHolder,
    sequence: Sequence[A]
  )
  case class DragFinished[A](
    holder: ViewHolder,
    sequence: Sequence[A]
  )
  trait OnDragListener[A]{ self =>

    def onStartDragging(event: DragStarted[A]): Unit

    def onFinishDragging(event: DragFinished[A]): Unit

    def append(listener: OnDragListener[A]): OnDragListener[A] =
      new OnDragListener[A] {
        override def onStartDragging(event: DragStarted[A]) = {
          self onStartDragging event
          listener onStartDragging event
        }
        override def onFinishDragging(event: DragFinished[A]) = {
          self onFinishDragging event
          listener onFinishDragging event
        }
      }
  }
  object OnDragListener {
    def onFinish[A](f: DragFinished[A] => Unit): OnDragListener[A] =
      new OnDragListener[A] {
        override def onStartDragging(event: DragStarted[A]): Unit = {
          //nop
        }
        override def onFinishDragging(event: DragFinished[A]): Unit = {
          f(event)
        }
      }
  }
}
