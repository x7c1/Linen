package x7c1.wheat.modern.database.selector.presets

import java.util
import java.util.Collections

import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper.{ACTION_STATE_DRAG, Callback, DOWN, UP}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.database.selector.presets.DraggableSequenceRoute.{DragFinished, DragStarted, OnDragListener}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanProvideSelector}
import x7c1.wheat.modern.sequence.{CanFilterFrom, Sequence}

import scala.collection.JavaConverters._
import scala.language.higherKinds

class DraggableSequenceRoute[I[T] <: CanIdentify[T], A] private (
  underlying: ClosableSequenceLoader[I, A], listener: OnDragListener[A]){

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
    }

  val callback: Callback =
    new Callback {
      override def getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder) = {
        Callback.makeFlag(ACTION_STATE_DRAG, UP | DOWN)
      }
      override def onSwiped(viewHolder: ViewHolder, direction: Int) = {
        // nop
      }
      override def onSelectedChanged(viewHolder: ViewHolder, actionState: Int) = {
        super.onSelectedChanged(viewHolder, actionState)
        actionState match {
          case ACTION_STATE_DRAG =>
            listener onStartDragging DragStarted(viewHolder)
          case _ =>
            // nop
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
        listener onFinishDragging DragFinished(viewHolder, orderedSequence)
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
  def apply[I[T] <: CanIdentify[T], A](db: SQLiteDatabase, listener: OnDragListener[A])
    (implicit
      x1: CanTraverse[I, A],
      x2: CanProvideSelector[A]{ type Selector <: TraverseOn[I, A] }
    ): DraggableSequenceRoute[I, A] = {

    new DraggableSequenceRoute[I, A](new ClosableSequenceLoaderImpl(db), listener)
  }
  case class DragStarted[A](
    holder: ViewHolder
  )
  case class DragFinished[A](
    holder: ViewHolder,
    sequence: Sequence[A]
  )
  trait OnDragListener[A]{
    def onStartDragging(event: DragStarted[A]): Unit
    def onFinishDragging(event: DragFinished[A]): Unit
  }
}
