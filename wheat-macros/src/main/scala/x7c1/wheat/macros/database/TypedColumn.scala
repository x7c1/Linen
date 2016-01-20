package x7c1.wheat.macros.database

import android.database.Cursor

import scala.collection.immutable.Nil
import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object TypedColumn {
  def apply[A](cursor: Cursor): CursorHolder[A] =  macro TypedColumnImpl.of[A]
  def get[A, B](holder: CursorHolder[A])(block: A => B): B = macro TypedColumnImpl.get[A, B]
}

trait ColumnDefinition {
  def long(name: String): Long = ???
  def string(name: String): String = ???
}

abstract class CursorHolder[A](cursor: Cursor) {
  def getColumnIndex(name: String): Int
  def getInt(index: Int): Int = cursor getInt index
  def getLong(index: Int): Long = cursor getLong index
  def getString(index: Int): String = cursor getString index
}

private object TypedColumnImpl {

  def of[A: c.WeakTypeTag](c: blackbox.Context)(cursor: c.Tree): c.Tree = {
    import c.universe._

    val tp = weakTypeOf[A]
    val xs = tp.members filter { symbol =>
      !symbol.fullName.startsWith("java.") &&
        !symbol.fullName.startsWith("scala.") &&
        !symbol.isConstructor && symbol.isMethod
    } map (_.asMethod) filter (_.paramLists.isEmpty)

    val indexes = xs map { method =>
      val key = method.name.toString
      q"lazy val ${TermName(key)} = $cursor.getColumnIndex($key)"
    }
    val cases = xs map { method =>
      val key = method.name.toString
      cq"""$key => this.${TermName(key)}"""
    }
    val holder = appliedType(typeOf[CursorHolder[_]].typeConstructor, tp)
    val tree =
      q"""
      new $holder($cursor){
        ..$indexes
        override def getColumnIndex(name: String) = name match {
          case ..$cases
          case _ => throw new IllegalArgumentException("unknown column: " + name)
        }
      }
    """
//    println(showCode(tree))
    tree
  }

  def get[A: c.WeakTypeTag, B: c.WeakTypeTag]
    (c: blackbox.Context)(holder: c.Tree)(block: c.Tree): c.Tree = {

    import c.universe._
    val column = block.children match {
      case Seq(_, z: Select) if z.symbol.isMethod => z.symbol.asMethod
      case Nil => throw new IllegalArgumentException("invalid form of expression")
    }
    val call = column.returnType match {
      case x if x =:= typeOf[String] => q"$holder.getString"
      case x if x =:= typeOf[Long] => q"$holder.getLong"
      case x if x =:= typeOf[Int] => q"$holder.getInt"
      case x =>
        throw new IllegalArgumentException(s"unsupported type: $x")
    }
    val key = column.name.toString
    val tree = q"""$call($holder.getColumnIndex($key))"""
//    println(showCode(tree))
    tree
  }
}
