package x7c1.wheat.modern.database.selector.presets

import android.database.sqlite.SQLiteDatabase
import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.TaskProvider.async
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{Done, LoaderEvent, SqlError}
import x7c1.wheat.modern.database.selector.presets.TraverseOn.TraverseOnProvidable
import x7c1.wheat.modern.database.selector.{CanExtract, CanIdentify, CanSelect, CursorReadable, CursorReifiable}
import x7c1.wheat.modern.sequence.{CanMapFrom, Sequence}

import scala.language.{higherKinds, reflectiveCalls}

trait CanTraverse[I[T] <: CanIdentify[T], A] extends CanExtract[I, ClosableSequence[A]]{
  override type Result[X] = Either[SQLException, X]
}

trait CanTraverseBySelect [I[T] <: CanIdentify[T], A]
  extends CanTraverse[I, A] with CanSelect[I, ClosableSequence[A]]{

  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor): Unit = {
    // nop
  }
}

trait ClosableSequence[+A] extends Sequence[A]{
  def closeCursor(): Unit
}

object ClosableSequence {
  def apply[
    FROM: CursorReifiable,
    TO: ({ type L[T] = CursorReadable[FROM, T] })#L](cursor: Cursor): ClosableSequence[TO] = {

    new ClosableSequence[TO] {
      lazy val read = implicitly[CursorReadable[FROM, TO]].readAt
      lazy val typed = implicitly[CursorReifiable[FROM]].reify(cursor)
      override def closeCursor() = cursor.close()
      override def findAt(position: Int) = read(typed, position)
      override def length = cursor.getCount
    }
  }
  implicit object canMapFrom extends CanMapFrom[ClosableSequence]{
    override def mapFrom[A, B](fa: ClosableSequence[A])(f: A => B): ClosableSequence[B] =
      new ClosableSequence[B] {
        override def closeCursor() = fa.closeCursor()
        override def findAt(position: Int) = fa.findAt(position) map f
        override def length = fa.length
      }
  }
}

class ClosableSequenceLoader[I[T] <: CanIdentify[T], A] private
  (db: SQLiteDatabase)(implicit x1: CanTraverse[I, A], x2: TraverseOnProvidable[I, A]){

  private val holder = new SequenceHolder

  val sequence: Sequence[A] = holder

  def startLoading[X: I](x: X): CallbackTask[LoaderEvent[A]] = async {
    db.selectorOf[A].traverseOn(x)
  } map {
    case Right(xs) =>
      holder.updateSequence(xs)
      Done(xs)
    case Left(e) =>
      SqlError(e)
  }
  private class SequenceHolder extends Sequence[A]{
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
  }
}

object ClosableSequenceLoader {
  sealed trait LoaderEvent[A]
  case class Done[A](sequence: ClosableSequence[A]) extends LoaderEvent[A]
  case class SqlError[A](cause: SQLException) extends LoaderEvent[A]

  def apply[I[T] <: CanIdentify[T], A]
    (db: SQLiteDatabase)(implicit
      x1: CanTraverse[I, A],
      x2: TraverseOnProvidable[I, A]): ClosableSequenceLoader[I, A] = {

    new ClosableSequenceLoader(db)
  }
}
