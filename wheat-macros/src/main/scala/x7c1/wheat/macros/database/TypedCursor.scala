package x7c1.wheat.macros.database

import android.content.ContentValues
import android.database.Cursor

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object TypedCursor {
  def apply[A <: TypedFields]
    (cursor: Cursor): A with TypedCursor[A] = macro TypedColumnImpl.create[A]
}

trait TypedCursor[X] {
  self: X =>

  def moveTo(n: Int): Boolean

  def moveToFind[A](n: Int)(f: => A): Option[A] = {
    if (moveTo(n)) Some(f)
    else None
  }
  def moveToHead[A](f: X => A): Option[A] = {
    if (moveTo(0)) Some(f(this))
    else None
  }
  def freezeAt(n: Int): Option[X]
}

trait TypedFields {
  type -->[A, B] = FieldTransform[A, B]
}

object TypedFields {

  def expose[A <: TypedFields]: A = macro TypedContentValues.extract[A]

  def toContentValues[A](pairs: A*): ContentValues = macro TypedContentValues.unwrap[A]

  def toArgs[A](pairs: A*): Seq[(String, String)] = macro TypedFieldsParser.toSelectionArgs[A]
}

trait FieldTransform[A, B]{
  def raw: A
  def typed: B
}

trait FieldConvertible[A, B]{
  def wrap(value: A): B
  def unwrap(value: B): A
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
      case x if x =:= typeOf[Double] => q"$cursor.getDouble($indexKey)"
      case x if x =:= typeOf[Option[Long]] =>
        /*
          cannot use cursor.getLong here
            because it returns 0 when target value is null
         */
        q"Option($cursor.getString($indexKey)).map(_.toLong)"

      case x if x =:= typeOf[Option[Int]] =>
        q"Option($cursor.getString($indexKey)).map(_.toInt)"

      case x if x <:< typeOf[FieldTransform[_, _]] =>
        val Seq(from, to) = tpe.typeArgs
        val value = getValue(from, indexKey)
        val convertible = appliedType(typeOf[FieldConvertible[_, _]].typeConstructor, from, to)
        val transform = appliedType(typeOf[FieldTransform[_, _]].typeConstructor, from, to)
        q"""
          new $transform {
            override val raw = $value
            override val typed = implicitly[$convertible].wrap($value)
          }"""

      case x =>
        throw new IllegalArgumentException(s"unsupported type: $x")
    }
    val kvs = methods map { method =>
      val key = method.name.toString
      val indexKey = TermName(c.freshName(key + "_index_"))
      val value = getValue(method.returnType, indexKey)
      (indexKey, key, value)
    }
    val overrides = kvs flatMap { case (indexKey, key, value) =>
      Seq(
        q"lazy val $indexKey = $cursor.getColumnIndexOrThrow($key)",
        q"override def ${TermName(key)} = $value"
      )
    }
    val freeze = kvs map {
      case (_, key, value) =>
        q"override val ${TermName(key)} = $value"
    }
    val typedCursor = appliedType(
      typeOf[TypedCursor[_]].typeConstructor, definition
    )
    val tree = q"""
      new $definition with $typedCursor {
        ..$overrides
        override def moveTo(n: Int) = $cursor.moveToPosition(n)
        override def freezeAt(n: Int) =
          if (moveTo(n)) Some(new $definition { ..$freeze })
          else None
      }
    """
//    println(showCode(tree))
    tree
  }
}
