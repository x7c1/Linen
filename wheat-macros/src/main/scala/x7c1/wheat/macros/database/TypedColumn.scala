package x7c1.wheat.macros.database

import android.database.Cursor

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object TypedColumn {
  def apply[A <: ColumnDefinition](cursor: Cursor): A = macro TypedColumnImpl.create[A]
}

trait ColumnDefinition {
  type -->[A, B] = ColumnTransform[A, B]
}

trait ColumnTransform[A, B]{
  def raw: A
  def typed: B
}

trait ColumnConvertible[A, B]{
  def convertFrom(value: A): B
}

private object TypedColumnImpl {
  def create[A: c.WeakTypeTag](c: blackbox.Context)(cursor: c.Tree): c.Tree = {
    import c.universe._

    val definition = weakTypeOf[A]
    val methods = definition.members filter { symbol =>
      !symbol.fullName.startsWith("java.") &&
      !symbol.fullName.startsWith("scala.") &&
      !symbol.isConstructor && symbol.isMethod
    } map (_.asMethod) filter (_.paramLists.isEmpty)

    def getValue(tpe: Type, indexKey: TermName): Tree = tpe match {
      case x if x =:= typeOf[String] => q"$cursor.getString($indexKey)"
      case x if x =:= typeOf[Long] => q"$cursor.getLong($indexKey)"
      case x if x =:= typeOf[Int] => q"$cursor.getInt($indexKey)"
      case x if x <:< typeOf[ColumnTransform[_, _]] =>
        val Seq(from, to) = tpe.typeArgs
        val value = getValue(from, indexKey)
        val convertible = appliedType(typeOf[ColumnConvertible[_, _]].typeConstructor, from, to)
        val transform = appliedType(typeOf[ColumnTransform[_, _]].typeConstructor, from, to)
        q"""
          new $transform {
            override def raw = $value
            override def typed = implicitly[$convertible].convertFrom($value)
          }"""

      case x =>
        throw new IllegalArgumentException(s"unsupported type: $x")
    }
    val overrides = methods flatMap { method =>
      val key = method.name.toString
      val indexKey = TermName(c.freshName(key + "_index_"))
      val value = getValue(method.returnType, indexKey)
      Seq(
        q"lazy val $indexKey = $cursor.getColumnIndex($key)",
        q"override def ${TermName(key)} = $value"
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
