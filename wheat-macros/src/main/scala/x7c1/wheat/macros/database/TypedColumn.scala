package x7c1.wheat.macros.database

import android.database.Cursor

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object TypedColumn {
  def apply[A <: ColumnDefinition](cursor: Cursor): A = macro TypedColumnImpl.create[A]
}

trait ColumnDefinition {
}

private object TypedColumnImpl {
  def create[A: c.WeakTypeTag](c: blackbox.Context)(cursor: c.Tree): c.Tree = {
    import c.universe._

    val definition = weakTypeOf[A]
    val xs = definition.members filter { symbol =>
      !symbol.fullName.startsWith("java.") &&
      !symbol.fullName.startsWith("scala.") &&
      !symbol.isConstructor && symbol.isMethod
    } map (_.asMethod) filter (_.paramLists.isEmpty)

    val overrides = xs flatMap { method =>
      val key = method.name.toString
      val call = method.returnType match {
        case x if x =:= typeOf[String] => q"$cursor.getString"
        case x if x =:= typeOf[Long] => q"$cursor.getLong"
        case x if x =:= typeOf[Int] => q"$cursor.getInt"
        case x =>
          throw new IllegalArgumentException(s"unsupported type: $x")
      }
      val indexKey = TermName(c.freshName(key + "_index_"))
      Seq(
        q"""lazy val $indexKey = $cursor.getColumnIndex($key)""",
        q"override def ${TermName(key)} = $call(this.$indexKey)"
      )
    }
    val tree = q"""
      new $definition {
        ..$overrides
      }
    """
//    println(showCode(tree))
    tree
  }
}
