package x7c1.wheat.modern.database.selector.presets

import java.util
import java.util.Collections

import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper.{ACTION_STATE_DRAG, Callback, DOWN, UP}
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.database.selector.{CanIdentify, CanProvideSelector}
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.LoaderEvent
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoaderToDrag.{DragFinished, DragStarted, OnDragListener}
import x7c1.wheat.modern.sequence.{CanFilterFrom, Sequence}

import scala.collection.breakOut
import scala.language.higherKinds

class ClosableSequenceLoaderToDrag[I[T] <: CanIdentify[T], A] private (
  loader: ClosableSequenceLoader[I, A]) extends ClosableSequenceLoader[I, A]{

  override def sequence: Sequence[A] = loader.sequence

  override def closeCursor(): Unit = loader.closeCursor()

  override def startLoading[X: I](x: X): CallbackTask[LoaderEvent[A]] = {
    for {
      event <- loader startLoading x
      _ <- task { positions = init() }
    } yield {
      event
    }
  }
  def callbackBy(listener: OnDragListener[A]): Callback = new Callback {
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
          listener onStartDragging DragStarted(viewHolder, sequence)
        case _ =>
        // nop
      }
    }
    override def onMove(recyclerView: RecyclerView, holder: ViewHolder, target: ViewHolder) = {
      val from = holder.getAdapterPosition
      val to = target.getAdapterPosition

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
    val map = (0 until positions.size()).map(n => n -> positions.get(n))(breakOut): Map[Int, Int]
    implicitly[CanFilterFrom[Sequence]].asFiltered(sequence)(map)
  }
  private var positions = init()

  private def init(): util.List[Int] = {
    util.Arrays.asList(0 until sequence.length :_*)
  }
}

object ClosableSequenceLoaderToDrag {
  def apply[I[T] <: CanIdentify[T], A](db: SQLiteDatabase)
    (implicit
      x1: CanTraverse[I, A],
      x2: CanProvideSelector[A]{ type Selector <: TraverseOn[I, A] }
    ): ClosableSequenceLoaderToDrag[I, A] = {

    new ClosableSequenceLoaderToDrag[I, A](new ClosableSequenceLoaderImpl(db))
  }
  case class DragStarted[A](
    holder: ViewHolder,
    sequence: Sequence[A]
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
