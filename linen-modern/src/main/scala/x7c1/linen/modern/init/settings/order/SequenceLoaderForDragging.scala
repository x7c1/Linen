package x7c1.linen.modern.init.settings.order

import java.util
import java.util.Collections

import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper.{ACTION_STATE_DRAG, Callback, DOWN, UP}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.LoaderEvent
import x7c1.wheat.modern.database.selector.presets.{CanTraverse, ClosableSequenceLoader, TraverseOn}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanProvideSelector}
import x7c1.wheat.modern.sequence.{CanFilterFrom, Sequence}

import scala.collection.breakOut
import scala.language.higherKinds

class SequenceLoaderForDragging[I[T] <: CanIdentify[T], A](
    db: SQLiteDatabase,
    onStart: PartialFunction[(ViewHolder, Sequence[A]), Unit],
    onFinish: PartialFunction[(ViewHolder, Sequence[A]), Unit])
  (implicit
    x1: CanTraverse[I, A],
    x2: CanProvideSelector[A]{ type Selector <: TraverseOn[I, A] }){

  private val loader = ClosableSequenceLoader[I, A](db)

  private var positions: util.List[Int] = init()

  private def init(): util.List[Int] = {
    util.Arrays.asList(0 until sequence.length :_*)
  }
  val callback: Callback = new Callback {
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
          val pair = viewHolder -> sequence
          if (onStart isDefinedAt pair){
            onStart(pair)
          } else {
            Log error s"[failed] unknown type of ViewHolder: $viewHolder"
          }
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
      val pair = viewHolder -> orderedSequence
      if (onFinish isDefinedAt pair){
        onFinish(pair)
      } else {
        Log error s"[failed] unknown type of ViewHolder: $viewHolder"
      }
    }
  }
  def sequence: Sequence[A] = {
    loader.sequence
  }
  def orderedSequence: Sequence[A] = {
    val map = (0 until positions.size()).map(n => n -> positions.get(n))(breakOut): Map[Int, Int]
    implicitly[CanFilterFrom[Sequence]].asFiltered(loader.sequence)(map)
  }
  def startLoading[X: I](x: X): CallbackTask[LoaderEvent[A]] =
    for {
      event <- loader.startLoading(x)
      _ <- task { positions = init() }
    } yield {
      event
    }
}
