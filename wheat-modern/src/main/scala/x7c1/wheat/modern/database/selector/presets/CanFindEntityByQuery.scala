package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.{CursorConverter, CursorConvertible, CursorReifiable, CanSelectDirectly}

import scala.language.reflectiveCalls

trait CanFindByQuery [A] extends CanSelectDirectly[Option[A]]{

  override type Result[X] = Either[SQLException, X]

  override def fromCursor(cursor: Cursor) = {
    Right(reify(cursor))
  }
  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor) = {
    cursor.close()
  }
  def reify(cursor: Cursor): Option[A]
}

abstract class CanFindEntityByQuery[
  FROM: CursorReifiable,
  TO: ({ type L[T] = CursorConvertible[FROM, T] })#L
] (query0: Query) extends CanFindByQuery[TO]{

  override def query: Query = query0

  def reify(cursor: Cursor): Option[TO] = {
    new CursorConverter[FROM, TO](cursor) convertAt 0
  }
}
