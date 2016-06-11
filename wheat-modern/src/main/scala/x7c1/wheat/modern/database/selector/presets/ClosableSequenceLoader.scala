package x7c1.wheat.modern.database.selector.presets

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.TaskProvider.async
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{Done, LoaderEvent, SqlError}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanProvideSelector}
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
