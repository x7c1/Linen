package x7c1.wheat.modern.database.selector.presets

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.reify.HasConstructor
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{LoadingDone, LoadingError, SqlError}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanProvideSelector}
import x7c1.wheat.modern.decorator.Imports.toRichView
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.kinds.FutureFate.HasContext
import x7c1.wheat.modern.kinds.{Fate, FutureFate}
import x7c1.wheat.modern.sequence.Sequence

import scala.language.higherKinds

abstract class ClosableSequenceLoader[C: HasContext, I[T] <: CanIdentify[T], A]{

  def startLoading[X: I](x: X): Fate[C, LoadingError, LoadingDone[A]]

  def sequence: Sequence[A]

  def closeCursor(): Unit
}

object ClosableSequenceLoader {

  case class LoadingDone[A](sequence: Sequence[A])

  trait LoadingError {
    def detail: String
  }
  object LoadingError {
    implicit object hasConstructor extends HasConstructor[Throwable => LoadingError]{
      override def newInstance = new UnexpectedError(_)
    }
  }
  case class SqlError(cause:  SQLException) extends LoadingError {
    override def detail: String = format(cause.getCause){"[failed]"}
  }
  case class UnexpectedError(cause: Throwable) extends LoadingError {
    override def detail: String = format(cause){"[failed]"}
  }
  def apply[C: HasContext, I[T] <: CanIdentify[T], A]
    (db: SQLiteDatabase)
    (implicit
      x1: CanTraverse[I, A],
      x2: CanProvideSelector[A]{ type Selector <: TraverseOn[I, A] }
    ): ClosableSequenceLoader[C, I, A] = {

    new ClosableSequenceLoaderImpl(db)
  }
}

private class ClosableSequenceLoaderImpl[C: HasContext, I[T] <: CanIdentify[T], A]
  (db: SQLiteDatabase)
  (implicit
    x1: CanTraverse[I, A],
    x2: CanProvideSelector[A]{ type Selector <: TraverseOn[I, A] }
  ) extends ClosableSequenceLoader[C, I, A]{

  private val holder = new SequenceHolder

  override val sequence: Sequence[A] = holder

  override def startLoading[X: I](x: X) = FutureFate[C, LoadingError, LoadingDone[A]] {
    db.selectorOf[A] traverseOn x match {
      case Right(xs) =>
        holder updateSequence xs
        Right(LoadingDone(xs))
      case Left(e) =>
        Left(SqlError(e))
    }
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

class RecyclerViewReloader[C: HasContext, I[T] <: CanIdentify[T], A](
  underlying: ClosableSequenceLoader[C, I, A],
  recyclerView: RecyclerView ) extends ClosableSequenceLoader[C, I, A]{

  def redrawBy[X: I](key: X): Fate[C, LoadingError, Unit] = {
    underlying startLoading key map { done =>
      recyclerView runUi {_.getAdapter.notifyDataSetChanged()}
    }
  }
  override def startLoading[X: I](x: X) = underlying startLoading x
  override def closeCursor(): Unit = underlying.closeCursor()
  override def sequence = underlying.sequence
}
