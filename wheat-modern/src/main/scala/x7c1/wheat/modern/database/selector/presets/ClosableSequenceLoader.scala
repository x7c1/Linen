package x7c1.wheat.modern.database.selector.presets

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.TaskProvider.async
import x7c1.wheat.modern.callback.either.EitherTask
import x7c1.wheat.modern.callback.either.EitherTask.|
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{Done, LoaderEvent, SqlError}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanProvideSelector}
import x7c1.wheat.modern.decorator.Imports.toRichView
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.sequence.Sequence

import scala.language.higherKinds

trait ClosableSequenceLoader[I[T] <: CanIdentify[T], A]{

  def startLoading[X: I](x: X): CallbackTask[LoaderEvent[A]]

  def sequence: Sequence[A]

  def closeCursor(): Unit
}

object ClosableSequenceLoader {

  sealed trait LoaderEvent[+A]

  case class Done[A](sequence: Sequence[A]) extends LoaderEvent[A]

  case class SqlError(cause: SQLException) extends LoaderEvent[Nothing]

  def apply[I[T] <: CanIdentify[T], A]
    (db: SQLiteDatabase)
    (implicit
      x1: CanTraverse[I, A],
      x2: CanProvideSelector[A]{ type Selector <: TraverseOn[I, A] }
    ): ClosableSequenceLoader[I, A] = {

    new ClosableSequenceLoaderImpl(db)
  }
}

private class ClosableSequenceLoaderImpl[I[T] <: CanIdentify[T], A]
  (db: SQLiteDatabase)
  (implicit
    x1: CanTraverse[I, A],
    x2: CanProvideSelector[A]{ type Selector <: TraverseOn[I, A] }
  ) extends ClosableSequenceLoader[I, A]{

  private val holder = new SequenceHolder

  override val sequence: Sequence[A] = holder

  override def startLoading[X: I](x: X): CallbackTask[LoaderEvent[A]] = async {
    db.selectorOf[A].traverseOn(x)
  } map {
    case Right(xs) =>
      holder.updateSequence(xs)
      Done(xs)
    case Left(e) =>
      SqlError(e)
  }
  override def closeCursor(): Unit = {
    holder.closeCursor()
  }
  private class SequenceHolder extends ClosableSequence[A]{
    private var underlying: Option[ClosableSequence[A]] = None

    def updateSequence(sequence: ClosableSequence[A]) = synchronized {
      underlying foreach {_.closeCursor()}
      underlying = Some(sequence)
    }
    override def length = {
      underlying map (_.length) getOrElse 0
    }
    override def findAt(position: Int) = {
      underlying flatMap (_ findAt position)
    }
    override def closeCursor() = synchronized {
      underlying foreach (_.closeCursor())
      underlying = None
    }
  }
}

class RecyclerViewReloader[I[T] <: CanIdentify[T], A](
  underlying: ClosableSequenceLoader[I, A],
  recyclerView: RecyclerView ) extends ClosableSequenceLoader[I, A]{

  def redrawBy[X: I](key: X): Unit = {
    taskToRedraw(key) run {
      case Right(_) => // nop
      case Left(e) => Log error format(e){"[failed]"}
    }
  }
  def taskToRedraw[X: I](key: X): SQLException | Unit = EitherTask {
    underlying startLoading key map {
      case Done(xs) =>
        recyclerView runUi {
          _.getAdapter.notifyDataSetChanged()
        }
        Right({})
      case SqlError(e) =>
        Left(e)
    }
  }
  override def startLoading[X: I](x: X) = underlying startLoading x
  override def closeCursor(): Unit = underlying.closeCursor()
  override def sequence = underlying.sequence
}
