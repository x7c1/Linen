package x7c1.wheat.modern.database.selector

import android.database.sqlite.SQLiteDatabase
import android.database.{Cursor, SQLException}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.Query

import scala.language.{higherKinds, reflectiveCalls}

trait CanExtract[I[T] <: CanIdentify[T], A]{
  type Result[_]
  def extract[X: I](db: SQLiteDatabase, id: X): Result[A]
}

trait CanSelect[I[T] <: CanIdentify[T], A] extends CanExtract[I, A]{
  def query[X: I](target: X): Query

  def fromCursor(cursor: Cursor): Result[A]

  def onException(e: SQLException): Result[A]

  def atFinal(cursor: Cursor): Unit

  override def extract[X: I](db: SQLiteDatabase, id: X): Result[A] =
    try {
      val query = this.query(id)
      val cursor = db.rawQuery(query.sql, query.selectionArgs)
      try fromCursor(cursor)
      finally atFinal(cursor)
    } catch {
      case e: SQLException => onException(e)
    }

}

trait CursorReadable[FROM, TO]{
  def readAt: (FROM with TypedCursor[FROM], Int) => Option[TO]
}

trait CursorConvertible[FROM, TO] extends CursorReadable[FROM, TO]{
  protected lazy val convert = fromCursor

  override def readAt = {
    case (cursor, position) => synchronized(cursor.moveToFind(position){
      convert(cursor)
    })
  }
  def fromCursor: FROM with TypedCursor[FROM] => TO
}

trait CursorReifiable[A] {
  def reify(cursor: Cursor): A with TypedCursor[A]
}

trait RecordReifiable[A] extends CursorReifiable[A] with CursorReadable[A, A]{
  override def readAt = _ freezeAt _
}
